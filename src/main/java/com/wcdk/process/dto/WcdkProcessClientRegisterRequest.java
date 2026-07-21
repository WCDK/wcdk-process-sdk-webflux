package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @version 1.0
 * @auther WCDK
 * @date 2026/7/16
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessClientRegisterRequest {

    private String clientId;

    private String clientName;

    private String callbackUrl;

    private String username;

    private String password;

    private String authFlg;

    private Set<String> processBeanNames;
}
