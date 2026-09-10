package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.support.ProcessBeanRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @auther WCDK
 *
 * @version 1.0
 **/
@RestController
@RequestMapping("/wcdk_process")
public class WcdkProcessBeanController {

    private final ProcessBeanRegistry processBeanRegistry;

    public WcdkProcessBeanController(ProcessBeanRegistry processBeanRegistry) {
        this.processBeanRegistry = processBeanRegistry;
    }

    @PostMapping("/{processBeanName}")
    public Mono<ApiResponse<Object>> invoke(@PathVariable String processBeanName,
                                            @RequestBody(required = false) WcdkProcessConnectionEvent request) {
        if (processBeanName.equals("register_bak")) {
            return Mono.just(ApiResponse.success(null));
        }
        WcdkProcessConnectionEvent event = request == null ? new WcdkProcessConnectionEvent() : request;
        if (!StringUtils.hasText(event.getProcessBeanName())) {
            event.setProcessBeanName(processBeanName);
        }
        return processBeanRegistry.invoke(processBeanName, event)
                .map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.success(null));
    }
}
