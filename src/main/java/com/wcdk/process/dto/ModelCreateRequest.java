package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCreateRequest {

    private String modelName;

    private String modelKey;

    private String category;

    private String bpmnXml;
}
