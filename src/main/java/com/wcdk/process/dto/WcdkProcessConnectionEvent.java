package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessConnectionEvent {

    private String connectionId;

    private String clientId;

    private String clientName;

    private String processInstanceId;

    private String processDefinitionKey;
    private String processDefinitionId;
    private String businessKey;

    private String processBeanName;

    private String eventType;

    private String message;

    private LocalDateTime eventTime;

    private Map<String, Object> payload;

    private String errorMessage;
}
