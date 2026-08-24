package com.example.wardrobe.common.exception;

import org.springframework.http.HttpStatus;

public class InternalErrorException extends BusinessException {

    public InternalErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
    }

    public InternalErrorException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
        initCause(cause);
    }
}
