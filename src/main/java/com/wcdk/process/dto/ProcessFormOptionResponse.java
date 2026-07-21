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
public class ProcessFormOptionResponse {

    private String label;

    private String value;
}
