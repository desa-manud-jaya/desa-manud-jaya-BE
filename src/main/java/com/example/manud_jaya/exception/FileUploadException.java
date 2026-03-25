package com.example.manud_jaya.exception;

public class FileUploadException extends RuntimeException {

    public static final int HTTP_STATUS = 400;

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
