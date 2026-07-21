package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @auther WCDK
 * @date 2026/7/20
 * @version 1.0
 **/
@Data
@Builder
public class ProcessActionButtonResponse {

    private String actionKey;

    private String label;

    private String buttonType;

    private Boolean submit;

    private Integer sortOrder;
}
