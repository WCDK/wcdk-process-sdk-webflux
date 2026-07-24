package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessClientDefinition {

    private String clientId;

    private String clientName;

    private String callbackUrl;

    private String serviceName;

    private Map<String, String> callbackHeaders;

    private Set<String> processBeanNames;
}
