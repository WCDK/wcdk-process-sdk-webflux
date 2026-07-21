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
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@AutoConfiguration
@EnableConfigurationProperties(WcdkProcessProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class WcdkProcessAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WcdkProcessConnectionConfig wcdkProcessConnectionConfig(WcdkProcessProperties properties) {
        return WcdkProcessConnectionConfig.builder()
                .clientId(properties.getClientId())
                .clientName(properties.getClientName())
                .endpoint(properties.getEndpoint())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .callbackUrl(properties.getCallbackUrl())
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
        return builder
                .baseUrl(trimTrailingSlash(serverConfig.getBaseUrl()))
                .build();
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
                                              WcdkProcessServerConfig serverConfig) {
        return new WcdkProcessClient(wcdkProcessWebClient, objectMapper, connectionConfig, serverConfig);
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
}
