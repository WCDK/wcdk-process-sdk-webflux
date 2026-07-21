package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDesignerExportEdgeRequest {

    private String id;

    private String sourceId;

    private String targetId;

    private String name;

    private String conditionExpression;
}
