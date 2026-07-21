package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/20
 * @version 1.0
 **/
@Data
@Builder
public class ProcessFormFieldResponse {

    private String fieldKey;

    private String label;

    private String componentType;

    private String dataType;

    private String placeholder;

    private Boolean required;

    private Boolean readOnly;

    private String defaultValue;

    private Integer rows;

    private Integer sortOrder;

    private String sourceNodeId;

    private String sourceNodeName;

    private List<ProcessFormOptionResponse> options;
}
