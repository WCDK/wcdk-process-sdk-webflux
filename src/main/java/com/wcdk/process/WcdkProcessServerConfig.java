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
public class WcdkProcessServerConfig {

    private final String baseUrl;

    private final String username;

    private final String password;

    private final Duration timeout;

    private final String authFlg;
}
