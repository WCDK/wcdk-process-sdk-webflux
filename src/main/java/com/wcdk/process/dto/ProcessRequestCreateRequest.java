package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ProcessRequestCreateRequest {

    private String processDefinitionKey;

    private String taskName;

    private Map<String, Object> formData;

    private Boolean submit;

    private String processBeanName;
}
