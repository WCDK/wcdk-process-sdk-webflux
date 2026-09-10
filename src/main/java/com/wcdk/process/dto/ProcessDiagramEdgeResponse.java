package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
@Builder
public class ProcessDiagramEdgeResponse {

    private String elementId;

    private String elementName;

    private String sourceRef;

    private String targetRef;

    private String conditionExpression;

    private List<ProcessDiagramWaypointResponse> waypoints;
}
