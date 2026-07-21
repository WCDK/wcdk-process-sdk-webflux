package com.wcdk.process.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private List<T> records;
}
