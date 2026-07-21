package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDesignerExportResponse {

    private String fileName;

    private String contentType;

    private String contentBase64;

    private List<String> skippedNodeLabels;
}
