package com.wcdk.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Validated
@ConfigurationProperties(prefix = "wcdk.process")
public class WcdkProcessProperties {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientName;

    @NotBlank
    private String endpoint;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String callbackUrl;

    private String serviceName;

    @NotNull
    private Long timeoutSeconds = 30L;

    @NotNull
    @Min(1)
    private Long activeReport = 10L;

    private String authFlg;
}
