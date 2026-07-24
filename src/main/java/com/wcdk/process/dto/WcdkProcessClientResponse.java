package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessClientResponse {

    private Long id;

    private String clientId;

    private String clientName;

    private String callbackUrl;

    private String serviceName;

    private String authFlg;

    private String clientStatus;

    private List<String> processBeanNames;

    private List<String> processNames;

    private Long processBeanCount;

    private Long processBindingCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
