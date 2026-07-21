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
public class ProcessDiagramNodeResponse {

    private String elementId;

    private String elementName;

    private String elementType;

    private String parentId;

    private String documentation;

    private String defaultFlowId;

    private Double x;

    private Double y;

    private Double width;

    private Double height;

    private Integer incomingCount;

    private Integer outgoingCount;
}
