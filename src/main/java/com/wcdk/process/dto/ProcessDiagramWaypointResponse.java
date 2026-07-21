package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class ProcessDiagramWaypointResponse {

    private Double x;

    private Double y;
}
