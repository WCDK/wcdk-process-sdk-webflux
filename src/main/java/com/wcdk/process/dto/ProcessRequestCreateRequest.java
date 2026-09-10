package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestCreateRequest {

    private String processDefinitionKey;

    private String processDefinitionId;

    private String requestId;

    private String businessKey;

    private String businessTitle;

    private String formVersionId;

    private Long expectedRevision;

    private String idempotencyKey;

    private String taskName;

    private Map<String, Object> formData;

    private Boolean submit;

    private String processBeanName;
}
