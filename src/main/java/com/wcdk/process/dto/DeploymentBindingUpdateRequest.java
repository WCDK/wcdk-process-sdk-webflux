package com.wcdk.process.dto;

import lombok.Data;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
public class DeploymentBindingUpdateRequest {

    private String clientId;

    private String processBeanName;
}
