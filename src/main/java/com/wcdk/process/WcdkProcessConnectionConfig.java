package com.wcdk.process;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Getter
@Builder
public class WcdkProcessConnectionConfig {

    private final String clientId;

    private final String clientName;

    private final String endpoint;

    private final String callbackUrl;

    private final String serviceName;

    private final String username;

    private final String password;

    private final Duration timeout;

    private final Duration activeReportInterval;

    private final String authFlg;


}
