package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
public class ModelResponse {

    private String modelId;

    private String modelName;

    private String modelKey;

    private String category;

    private String processBeanName;

    private Integer version;

    private String deploymentId;

    private Date createTime;

    private Date lastUpdateTime;
}
