package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRequest {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 办理变量
     */
    private Map<String, Object> variables;
}
