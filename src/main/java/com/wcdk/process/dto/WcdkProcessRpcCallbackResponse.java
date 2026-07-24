package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/23
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessRpcCallbackResponse {

    private String clientId;

    private String clientName;

    private String processBeanName;

    private Boolean success;

    private Object data;

    private String message;
}
