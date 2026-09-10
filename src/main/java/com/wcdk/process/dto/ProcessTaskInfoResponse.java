package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTaskInfoResponse {

    private String taskId;

    private String taskDefinitionKey;

    private String taskName;

    private String assignee;

    private String processInstanceId;

    private String processDefinitionId;

    private Long processRequestId;

    /**
     * 鍏宠仈琛ㄥ崟鏁版嵁銆?     */
    private Map<String, Object> relatedFormData;

    /**
     * 鍏宠仈琛ㄥ崟ID銆?     */
    private Long relatedFormId;

    /**
     * 鍏宠仈琛ㄥ崟鍚嶇О銆?     */
    private String relatedFormName;

    /**
     * 褰撳墠浠诲姟鍏宠仈琛ㄥ崟鍒楄〃銆?     */
    private List<Map<String, Object>> relatedForms;

    /**
     * 瀹℃壒鏃堕棿銆?     */
    private LocalDateTime eventTime;
}
