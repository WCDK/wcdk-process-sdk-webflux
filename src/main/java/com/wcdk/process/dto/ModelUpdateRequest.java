package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUpdateRequest {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型分类
     */
    private String category;

    /**
     * BPMN XML 内容
     */
    private String bpmnXml;
}
