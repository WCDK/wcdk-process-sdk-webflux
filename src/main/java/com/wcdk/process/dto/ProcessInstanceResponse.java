package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
public class ProcessInstanceResponse {

    private String processInstanceId;

    private String processDefinitionId;

    private String processDefinitionKey;

    private String businessKey;

    private String processBeanName;

    private Boolean suspended;
}
