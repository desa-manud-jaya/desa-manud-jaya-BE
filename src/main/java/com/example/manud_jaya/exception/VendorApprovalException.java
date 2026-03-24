package com.example.manud_jaya.exception;

public class VendorApprovalException extends RuntimeException {

    public static final int HTTP_STATUS = 403;

    public VendorApprovalException(String message) {
        super(message);
    }

    public VendorApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
