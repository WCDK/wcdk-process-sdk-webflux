package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartProcessRequest {

    /**
     * 流程定义标识，默认使用示例流程 leave-process
     */
    private String processDefinitionKey;

    /**
     * 业务主键
     */
    private String businessKey;

    /**
     * 流程发起人
     */
    private String starter;

    /**
     * 流程回调bean名称
     */
    private String processBeanName;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;
}
