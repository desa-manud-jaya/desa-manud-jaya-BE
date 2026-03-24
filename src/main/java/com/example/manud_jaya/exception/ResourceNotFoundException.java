package com.example.manud_jaya.exception;

public class ResourceNotFoundException extends RuntimeException {

    public static final int HTTP_STATUS = 404;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
