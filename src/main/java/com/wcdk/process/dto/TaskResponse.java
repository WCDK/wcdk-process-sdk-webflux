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
public class TaskResponse {

    private String taskId;

    private String taskName;

    private String currentTaskName;

    private List<ProcessTaskInfoResponse> parallelTasks;

    private String assignee;

    private String processInstanceId;

    private String processDefinitionId;

    private Long processRequestId;
}
