package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
public class ProcessRequestResponse {

    private Long id;

    private String processNo;

    private String starter;

    private String taskName;

    private String businessTitle;

    private Map<String, Object> formData;

    private String status;

    private String processInstanceId;

    private String currentTaskId;

    private String currentTaskName;

    private String processDefinitionKey;

    private String processDefinitionId;

    private String processBeanName;

    private List<String> activeNodeIds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
