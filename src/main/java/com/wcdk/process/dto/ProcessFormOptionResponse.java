package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
@Builder
public class ProcessFormOptionResponse {

    private String label;

    private String value;
}
