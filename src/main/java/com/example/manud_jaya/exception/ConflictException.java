package com.example.manud_jaya.exception;

public class ConflictException extends RuntimeException {

    public static final int HTTP_STATUS = 409;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
