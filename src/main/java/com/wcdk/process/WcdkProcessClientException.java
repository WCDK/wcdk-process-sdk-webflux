package com.wcdk.process;

/**
 * @auther WCDK
 *
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
