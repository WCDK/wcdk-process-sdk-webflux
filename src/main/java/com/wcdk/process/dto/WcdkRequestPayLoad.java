package com.wcdk.process.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WcdkRequestPayLoad {
    /**
     * 主键
     */
    private long id;
    /**
     * 流程编号
     */
    private String processNo;
    /**
     * 启动人
     */
    private String starter;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 业务标题
     */
    private String businessTitle;
    /**
     * 状态
     */
    private String status;
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    /**
     * 当前任务ID
     */
    private String currentTaskId;
    /**
     * 当前任务名称
     */
    private String currentTaskName;
    /**
     * 活跃节点ID列表
     */
    private List<String> activeNodeIds;
    /**
     * 表单数据
     */
    private Map<String, Object> formData;
    /**
     * 流程Bean名称
     */
    private String processBeanName;
}
