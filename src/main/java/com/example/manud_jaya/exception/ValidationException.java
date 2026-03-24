package com.example.manud_jaya.exception;

public class ValidationException extends RuntimeException {

    public static final int HTTP_STATUS = 400;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
