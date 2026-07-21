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
public class ProcessDefinitionDetailResponse {

    private String processDefinitionId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String category;

    private Integer version;

    private String deploymentId;

    private String deploymentName;

    private String resourceName;

    private Boolean suspended;

    private Integer nodeCount;

    private Integer userTaskCount;

    private Integer sequenceFlowCount;

    private String bpmnXml;

    private List<ProcessFormFieldResponse> formFields;

    private List<ProcessActionButtonResponse> actionButtons;

    private List<ProcessDiagramNodeResponse> nodes;

    private List<ProcessDiagramEdgeResponse> sequenceFlows;

    private List<String> activeNodeIds;
}
