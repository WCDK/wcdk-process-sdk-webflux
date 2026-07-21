package com.wcdk.process.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Integer code;

    private String message;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "处理成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
}
