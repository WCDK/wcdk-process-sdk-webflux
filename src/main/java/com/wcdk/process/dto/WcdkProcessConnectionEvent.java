package com.wcdk.process.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WcdkProcessConnectionEvent {

    /**
     * 客户端连接记录ID。
     */
    private String connectionId;

    /**
     * 客户端标识。
     */
    private String clientId;

    /**
     * 客户端名称。
     */
    private String clientName;

    /**
     * 流程实例ID。
     */
    private String processInstanceId;

    /**
     * 流程定义标识。
     */
    private String processDefinitionKey;

    /**
     * 流程定义ID。
     */
    private String processDefinitionId;

    /**
     * 流程定义名称。
     */
    private String processDefinitionName;

    /**
     * 业务主键。
     */
    private String businessKey;

    private String approvalId;

    private String approvalName;

    private String currentTaskId;

    private String currentTaskName;


    /**
     * 当前任务集
     * **/
    private List<ProcessTaskInfoResponse> currentTasks;

    private Boolean taskApproved;

    private String taskApprovalResult;


    /**
     * 当前总审批结果，0表示通过，1表示未通过，2表示驳回。
     */
    private Integer currentApprovalResult;

    private Map<String, Object> relatedFormData;

    private Long relatedFormId;

    private String relatedFormName;

    private List<Map<String, Object>> relatedForms;

    private String nextTaskId;

    private String nextTaskName;


    /**
     * 当前任务总审批进度，0 进行中  1已完成。
     */
    /**
     * 下一任务集。
     */
    private List<ProcessTaskInfoResponse> nextTasks;

    /**
     * 客户端流程处理器名称。
     */
    private String processBeanName;

    /**
     * 回调事件类型。
     */
    private String eventType;

    /**
     * 回调消息。
     */
    private String message;

    /**
     * 完成时间 当前任务所有节点审批完成后。
     */
    private LocalDateTime eventTime;

    /**
     * 错误消息。
     */
    private String errorMessage;

}