package com.wcdk.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.config.WcdkProcessAuthWebFilter;
import com.wcdk.process.controller.WcdkProcessBeanController;
import com.wcdk.process.support.WcdkProcessClientAutoRegisterRunner;
import com.wcdk.process.support.ProcessBeanRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@AutoConfiguration
@EnableConfigurationProperties(WcdkProcessProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class WcdkProcessAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcessConnectionConfig wcdkProcessConnectionConfig(WcdkProcessProperties properties, Environment environment) {
        return WcdkProcessConnectionConfig.builder()
                .clientId(properties.getClientId())
                .clientName(properties.getClientName())
                .endpoint(properties.getEndpoint())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .callbackUrl(properties.getCallbackUrl())
                .serviceName(resolveServiceName(properties, environment))
                .authFlg(properties.getAuthFlg())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .activeReportInterval(Duration.ofSeconds(properties.getActiveReport()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcessServerConfig wcdkProcessServerConfig(WcdkProcessProperties properties) {
        return WcdkProcessServerConfig.builder()
                .baseUrl(properties.getEndpoint())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .authFlg(properties.getAuthFlg())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder wcdkProcessWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClient wcdkProcessWebClient(WebClient.Builder builder, WcdkProcessServerConfig serverConfig) {
        if (isLoadBalancerEndpoint(serverConfig.getBaseUrl())) {
            return builder.build();
        }
        return builder.baseUrl(trimTrailingSlash(serverConfig.getBaseUrl())).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper wcdkProcessObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcessClient wcdkProcessClient(WebClient wcdkProcessWebClient,
                                              ObjectMapper objectMapper,
                                              WcdkProcessConnectionConfig connectionConfig,
                                              WcdkProcessServerConfig serverConfig,
                                              ObjectProvider<ReactiveLoadBalancer.Factory<ServiceInstance>> loadBalancerFactoryProvider) {
        return new WcdkProcessClient(wcdkProcessWebClient, objectMapper, connectionConfig, serverConfig, loadBalancerFactoryProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcessFlowClient wcdkProcessFlowClient(WcdkProcessClient wcdkProcessClient) {
        return new WcdkProcessFlowClient(wcdkProcessClient);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean
    public ProcessBeanRegistry processBeanRegistry(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        return new ProcessBeanRegistry(applicationContext, objectMapper);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean
    public WcdkProcessBeanController wcdkProcessBeanController(ProcessBeanRegistry processBeanRegistry) {
        return new WcdkProcessBeanController(processBeanRegistry);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean
    public WcdkProcessAuthWebFilter wcdkProcessAuthWebFilter(WcdkProcessConnectionConfig connectionConfig) {
        return new WcdkProcessAuthWebFilter(connectionConfig);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnMissingBean(name = "wcdkProcessClientAutoRegisterRunner")
    public WcdkProcessClientAutoRegisterRunner wcdkProcessClientAutoRegisterRunner(WcdkProcessClient wcdkProcessClient,
                                                                                   ProcessBeanRegistry processBeanRegistry,
                                                                                   WcdkProcessConnectionConfig connectionConfig) {
        return new WcdkProcessClientAutoRegisterRunner(wcdkProcessClient, processBeanRegistry, connectionConfig);
    }

    private String trimTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private boolean isLoadBalancerEndpoint(String value) {
        return StringUtils.hasText(value) && value.trim().startsWith("lb://");
    }

    private String resolveServiceName(WcdkProcessProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getServiceName())) {
            return properties.getServiceName().trim();
        }
        return environment.getProperty("spring.application.name");
    }
}
