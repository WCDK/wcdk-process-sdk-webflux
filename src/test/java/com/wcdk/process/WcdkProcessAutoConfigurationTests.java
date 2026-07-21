package com.wcdk.process;

import com.wcdk.process.annotation.ProcessBean;
import com.wcdk.process.config.WcdkProcessAuthWebFilter;
import com.wcdk.process.controller.WcdkProcessBeanController;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.support.ProcessBeanRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WcdkProcessAutoConfigurationTests {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebFluxAutoConfiguration.class, WcdkProcessAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "wcdk.process.client-id=demo-client",
                    "wcdk.process.client-name=流程演示系统",
                    "wcdk.process.endpoint=http://localhost:58082",
                    "wcdk.process.username=admin",
                    "wcdk.process.password=admin123",
                    "wcdk.process.timeout-seconds=30",
                    "wcdk.process.active-report=10"
            );

    @Test
    void shouldUseHighestPrecedenceForAutoConfigurationRegistration() {
        AutoConfigureOrder autoConfigureOrder = WcdkProcessAutoConfiguration.class.getAnnotation(AutoConfigureOrder.class);
        assertThat(autoConfigureOrder).isNotNull();
        assertThat(autoConfigureOrder.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void shouldBindWcdkProcessPropertiesAndExposeProcessBeanEndpoint() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WcdkProcessClient.class);
            assertThat(context).hasSingleBean(WcdkProcessConnectionConfig.class);
            assertThat(context).hasSingleBean(WcdkProcessServerConfig.class);
            assertThat(context).hasSingleBean(WcdkProcessFlowClient.class);
            assertThat(context).hasSingleBean(ProcessBeanRegistry.class);
            assertThat(context).hasSingleBean(WcdkProcessBeanController.class);
            assertThat(context).hasSingleBean(WcdkProcessAuthWebFilter.class);

            WcdkProcessConnectionConfig config = context.getBean(WcdkProcessConnectionConfig.class);
            WcdkProcessServerConfig serverConfig = context.getBean(WcdkProcessServerConfig.class);
            assertThat(config.getClientId()).isEqualTo("demo-client");
            assertThat(config.getClientName()).isEqualTo("流程演示系统");
            assertThat(config.getEndpoint()).isEqualTo("http://localhost:58082");
            assertThat(config.getTimeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.getActiveReportInterval()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.getAuthFlg()).isNull();
            assertThat(serverConfig.getBaseUrl()).isEqualTo("http://localhost:58082");
            assertThat(serverConfig.getUsername()).isEqualTo("admin");
            assertThat(serverConfig.getPassword()).isEqualTo("admin123");
            assertThat(context.getBean(ProcessBeanRegistry.class).getProcessBeanNames()).containsExactly("demoProcess");
        });
    }

    @Test
    void shouldInvokeAnnotatedMethodThroughUnifiedPrefixEndpoint() {
        contextRunner.run(context -> {
            WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();
            webTestClient.post()
                    .uri("/wcdk_process/demoProcess")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "businessKey":"BUS-001",
                              "message":"流程回调测试"
                            }
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo(200)
                    .jsonPath("$.message").isEqualTo("处理成功")
                    .jsonPath("$.data").isEqualTo("BUS-001");

            TestProcessBeanHandler handler = context.getBean(TestProcessBeanHandler.class);
            assertThat(handler.getLastEvent().get()).isNotNull();
            assertThat(handler.getLastEvent().get().getProcessBeanName()).isEqualTo("demoProcess");
            assertThat(handler.getLastEvent().get().getBusinessKey()).isEqualTo("BUS-001");
        });
    }

    @Test
    void shouldRejectCallbackWhenWcdkAuthHeaderDoesNotMatchConfiguredValue() {
        contextRunner.withPropertyValues("wcdk.process.auth-flg=demo-auth")
                .run(context -> {
                    WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();
                    webTestClient.post()
                            .uri("/wcdk_process/demoProcess")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                                    {
                                      "businessKey":"BUS-001"
                                    }
                                    """)
                            .exchange()
                            .expectStatus().isUnauthorized()
                            .expectBody()
                            .jsonPath("$.code").isEqualTo(401)
                            .jsonPath("$.message").isEqualTo("回调鉴权失败");
                });
    }

    @Test
    void shouldInvokeCallbackWhenWcdkAuthHeaderMatchesConfiguredValue() {
        contextRunner.withPropertyValues("wcdk.process.auth-flg=demo-auth")
                .run(context -> {
                    WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();
                    webTestClient.post()
                            .uri("/wcdk_process/demoProcess")
                            .header("WCDK_AUTH", "demo-auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                                    {
                                      "businessKey":"BUS-002"
                                    }
                                    """)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .jsonPath("$.code").isEqualTo(200)
                            .jsonPath("$.data").isEqualTo("BUS-002");
                });
    }

    @Test
    void shouldInvokeAnnotatedMethodWithPayloadRequestParameter() {
        contextRunner.withUserConfiguration(PayloadTestConfiguration.class)
                .run(context -> {
                    WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();
                    webTestClient.post()
                            .uri("/wcdk_process/payloadProcess")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                                    {
                                      "payload":{
                                        "processNo":"LC-001",
                                        "formData":{
                                          "amount":100
                                        }
                                      }
                                    }
                                    """)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .jsonPath("$.code").isEqualTo(200)
                            .jsonPath("$.data").isEqualTo("LC-001");

                    PayloadProcessBeanHandler handler = context.getBean(PayloadProcessBeanHandler.class);
                    assertThat(handler.getLastRequest().get().getProcessNo()).isEqualTo("LC-001");
                    assertThat(handler.getLastRequest().get().getFormData()).containsEntry("amount", 100);
                });
    }

    @Test
    void shouldInvokeReactiveAnnotatedMethod() {
        contextRunner.withUserConfiguration(ReactiveTestConfiguration.class)
                .run(context -> {
                    WebTestClient webTestClient = WebTestClient.bindToApplicationContext(context).build();
                    webTestClient.post()
                            .uri("/wcdk_process/reactiveProcess")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("""
                                    {
                                      "businessKey":"BUS-003"
                                    }
                                    """)
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .jsonPath("$.code").isEqualTo(200)
                            .jsonPath("$.data").isEqualTo("BUS-003");
                });
    }

    @Test
    void shouldRequireCompleteBindingWhenDeployModel() {
        contextRunner.run(context -> {
            WcdkProcessFlowClient flowClient = context.getBean(WcdkProcessFlowClient.class);

            StepVerifier.create(flowClient.deployModel("model-001", "demo-client", ""))
                    .expectErrorSatisfies(exception -> assertThat(exception)
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessage("选择客户端时必须指定processName"))
                    .verify();
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        TestProcessBeanHandler testProcessBeanHandler() {
            return new TestProcessBeanHandler();
        }
    }

    static class TestProcessBeanHandler {

        private final AtomicReference<WcdkProcessConnectionEvent> lastEvent = new AtomicReference<>();

        @ProcessBean("demoProcess")
        public String handle(WcdkProcessConnectionEvent event) {
            lastEvent.set(event);
            return event.getBusinessKey();
        }

        public AtomicReference<WcdkProcessConnectionEvent> getLastEvent() {
            return lastEvent;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class PayloadTestConfiguration {

        @Bean
        PayloadProcessBeanHandler payloadProcessBeanHandler() {
            return new PayloadProcessBeanHandler();
        }
    }

    static class PayloadProcessBeanHandler {

        private final AtomicReference<PayloadRequest> lastRequest = new AtomicReference<>();

        @ProcessBean("payloadProcess")
        public String handle(PayloadRequest request) {
            lastRequest.set(request);
            return request.getProcessNo();
        }

        public AtomicReference<PayloadRequest> getLastRequest() {
            return lastRequest;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ReactiveTestConfiguration {

        @Bean
        ReactiveProcessBeanHandler reactiveProcessBeanHandler() {
            return new ReactiveProcessBeanHandler();
        }
    }

    static class ReactiveProcessBeanHandler {

        @ProcessBean("reactiveProcess")
        public Mono<String> handle(WcdkProcessConnectionEvent event) {
            return Mono.just(event.getBusinessKey());
        }
    }

    static class PayloadRequest {

        private String processNo;

        private Map<String, Object> formData;

        public String getProcessNo() {
            return processNo;
        }

        public void setProcessNo(String processNo) {
            this.processNo = processNo;
        }

        public Map<String, Object> getFormData() {
            return formData;
        }

        public void setFormData(Map<String, Object> formData) {
            this.formData = formData;
        }
    }
}
