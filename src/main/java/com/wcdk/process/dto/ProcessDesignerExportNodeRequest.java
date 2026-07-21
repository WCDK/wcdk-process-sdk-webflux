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
public class ProcessDesignerExportNodeRequest {

    private String id;

    private String type;

    private String bpmnType;

    private String kind;

    private String label;

    private String shortLabel;

    private String name;

    private String code;

    private String documentation;

    private String defaultFlowId;

    private String parentId;

    private Boolean expanded;

    private Integer width;

    private Integer height;

    private Integer x;

    private Integer y;
}
