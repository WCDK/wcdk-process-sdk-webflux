package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
public class DeploymentResponse {

    private String deploymentId;

    private String deploymentName;

    private String fileName;

    private String category;

    private Date deployTime;

    private List<String> clientIds;

    private List<String> clientNames;

    private List<String> processBeanNames;
}
