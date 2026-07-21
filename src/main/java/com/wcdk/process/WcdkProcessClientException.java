package com.wcdk.process;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessClientException extends RuntimeException {

    public WcdkProcessClientException(String message) {
        super(message);
    }

    public WcdkProcessClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
