package com.wcdk.process.dto;

import lombok.Data;

/**
 * @auther WCDK
 * @date 2026/7/17
 * @version 1.0
 **/
@Data
public class DeploymentBindingUpdateRequest {

    private String clientId;

    private String processBeanName;
}
