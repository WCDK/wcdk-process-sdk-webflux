package com.wcdk.process.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.WcdkProcessAutoConfiguration;
import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
class WcdkProcessClientAutoRegisterRunnerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ApplicationArguments applicationArguments = new DefaultApplicationArguments(new String[0]);

    @Test
    void shouldAutoRegisterProcessBeanAfterStartup() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        CountDownLatch registerLatch = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sdk/wcdkprocess/clients/register",
                exchange -> handleRegister(exchange, requestBodyRef, null, registerLatch));
        server.start();
        try {
            int port = server.getAddress().getPort();
            new ReactiveWebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(WebFluxAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
                    .withUserConfiguration(TestConfiguration.class)
                    .withPropertyValues(
                            "wcdk.process.client-id=demo-client",
                            "wcdk.process.client-name=流程演示系统",
                            "wcdk.process.endpoint=http://127.0.0.1:" + port,
                            "wcdk.process.username=admin",
                            "wcdk.process.password=admin123",
                            "wcdk.process.timeout-seconds=30",
                            "wcdk.process.callback-url=http://127.0.0.1:58083"
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        context.getBean(org.springframework.boot.ApplicationRunner.class).run(applicationArguments);
                        assertThat(registerLatch.await(3, TimeUnit.SECONDS)).isTrue();
                        assertThat(requestBodyRef.get()).isNotBlank();
                        JsonNode root = objectMapper.readTree(requestBodyRef.get());
                        assertThat(root.get("clientId").asText()).isEqualTo("demo-client");
                        assertThat(root.get("clientName").asText()).isEqualTo("流程演示系统");
                        assertThat(root.get("callbackUrl").asText()).isEqualTo("http://127.0.0.1:58083");
                        assertThat(root.get("processBeanNames").isArray()).isTrue();
                        assertThat(root.get("processBeanNames")).hasSize(1);
                        assertThat(root.get("processBeanNames").get(0).asText()).isEqualTo("demoProcess");
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldAutoRegisterProcessBeanWithSpringCloudLoadBalancerEndpoint() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        CountDownLatch registerLatch = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/wcdk/sdk/wcdkprocess/clients/register",
                exchange -> handleRegister(exchange, requestBodyRef, null, registerLatch));
        server.start();
        try {
            int port = server.getAddress().getPort();
            new ReactiveWebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(WebFluxAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
                    .withUserConfiguration(TestConfiguration.class, LoadBalancerTestConfiguration.class)
                    .withPropertyValues(
                            "wcdk.process.client-id=demo-client",
                            "wcdk.process.client-name=流程演示系统",
                            "wcdk.process.endpoint=lb://wcdk-process-server/wcdk",
                            "wcdk.process.username=admin",
                            "wcdk.process.password=admin123",
                            "wcdk.process.timeout-seconds=30",
                            "wcdk.process.callback-url=http://127.0.0.1:58083",
                            "test.wcdk-process.port=" + port
                    )
                    .run(context -> {
                        assertThat(context).hasNotFailed();
                        context.getBean(org.springframework.boot.ApplicationRunner.class).run(applicationArguments);
                        assertThat(registerLatch.await(3, TimeUnit.SECONDS)).isTrue();
                        assertThat(requestBodyRef.get()).isNotBlank();
                        JsonNode root = objectMapper.readTree(requestBodyRef.get());
                        assertThat(root.get("clientId").asText()).isEqualTo("demo-client");
                        assertThat(root.get("processBeanNames").get(0).asText()).isEqualTo("demoProcess");
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldSendRegisterInfoByActiveReportInterval() throws Exception {
        AtomicReference<String> requestBodyRef = new AtomicReference<>();
        AtomicInteger requestCount = new AtomicInteger();
        CountDownLatch secondRegisterLatch = new CountDownLatch(2);
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sdk/wcdkprocess/clients/register",
                exchange -> handleRegister(exchange, requestBodyRef, requestCount, secondRegisterLatch));
        server.start();
        try {
            int port = server.getAddress().getPort();
            new ReactiveWebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(WebFluxAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
                    .withUserConfiguration(TestConfiguration.class)
                    .withPropertyValues(
                            "wcdk.process.client-id=demo-client",
                            "wcdk.process.client-name=流程演示系统",
                            "wcdk.process.endpoint=http://127.0.0.1:" + port,
                            "wcdk.process.username=admin",
                            "wcdk.process.password=admin123",
                            "wcdk.process.timeout-seconds=30",
                            "wcdk.process.active-report=1",
                            "wcdk.process.callback-url=http://127.0.0.1:58083"
                    )
                    .run(context -> {
                        context.getBean(org.springframework.boot.ApplicationRunner.class).run(applicationArguments);
                        assertThat(secondRegisterLatch.await(3, TimeUnit.SECONDS)).isTrue();
                        assertThat(requestCount.get()).isGreaterThanOrEqualTo(2);
                    });
        } finally {
            server.stop(0);
        }
    }

    private void handleRegister(HttpExchange exchange,
                                AtomicReference<String> requestBodyRef,
                                AtomicInteger requestCount,
                                CountDownLatch countDownLatch) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            requestBodyRef.set(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        if (requestCount != null) {
            requestCount.incrementAndGet();
        }
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
        byte[] responseBytes = """
                {"code":200,"message":"处理成功","data":null}
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        TestProcessBeanHandler testProcessBeanHandler() {
            return new TestProcessBeanHandler();
        }
    }

    static class TestProcessBeanHandler {

        @ProcessBean("demoProcess")
        public String handle(WcdkProcessConnectionEvent event) {
            return event == null ? "" : event.getBusinessKey();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class LoadBalancerTestConfiguration {

        @Bean
        ReactiveLoadBalancer.Factory<ServiceInstance> testReactiveLoadBalancerFactory(org.springframework.core.env.Environment environment) {
            int port = Integer.parseInt(environment.getRequiredProperty("test.wcdk-process.port"));
            ServiceInstance serviceInstance = new DefaultServiceInstance(
                    "wcdk-process-server-1",
                    "wcdk-process-server",
                    "127.0.0.1",
                    port,
                    false
            );
            return new ReactiveLoadBalancer.Factory<>() {
                @Override
                public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
                    return request -> reactor.core.publisher.Mono.just(new DefaultResponse(serviceInstance));
                }

                @Override
                public <X> Map<String, X> getInstances(String name, Class<X> type) {
                    return Collections.emptyMap();
                }

                @Override
                public <X> X getInstance(String name, Class<?> clazz, Class<?>... generics) {
                    return null;
                }

                @Override
                public LoadBalancerProperties getProperties(String serviceId) {
                    return new LoadBalancerProperties();
                }
            };
        }
    }
}
