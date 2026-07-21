package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
public class ProcessDefinitionResponse {

    private String processDefinitionId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String category;

    private Integer version;

    private String deploymentId;

    private String resourceName;

    private Boolean suspended;

    private List<String> clientIds;

    private List<String> clientNames;

    private List<String> processBeanNames;
}
