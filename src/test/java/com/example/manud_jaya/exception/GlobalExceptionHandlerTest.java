package com.example.manud_jaya.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeExceptionWithMessageShouldReturnThatMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");

        var response = handler.handleRuntimeException(new RuntimeException("Invalid password"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Invalid password", response.getBody().getMessage());
        assertEquals("/auth/login", response.getBody().getPath());
    }

    @Test
    void runtimeExceptionWithNullMessageShouldReturnDefaultMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");

        var response = handler.handleRuntimeException(new RuntimeException((String) null), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void runtimeExceptionWithBlankMessageShouldReturnDefaultMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");

        var response = handler.handleRuntimeException(new RuntimeException("   "), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void genericExceptionShouldKeepDefaultMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/any");

        var response = handler.handleGenericException(new Exception("Should not be exposed"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/any", response.getBody().getPath());
    }
}
