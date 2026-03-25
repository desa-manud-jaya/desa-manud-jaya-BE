package com.example.manud_jaya.exception;

public class UnauthorizedException extends RuntimeException {

    public static final int HTTP_STATUS = 401;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
