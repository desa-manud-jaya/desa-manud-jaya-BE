package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.GuideRegisterRequest;
import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        LoginResponse expected = new LoginResponse("1", "token123", "testuser", "USER");

        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertNotNull(response.getBody());
        assertEquals("token123", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("USER", response.getBody().getRole());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void registerUserCallsService() {
        UserRegisterRequest request = new UserRegisterRequest("user1", "user1@mail.com", "secret");

        ResponseEntity<?> response = authController.registerUser(request);

        verify(authService, times(1)).registerUser(request);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void registerVendorCallsService() {
        VendorRegisterRequest request = new VendorRegisterRequest();
        request.setUsername("vendor1");
        request.setEmail("vendor1@mail.com");
        request.setPassword("secret");

        ResponseEntity<?> response = authController.registerVendor(request);

        verify(authService, times(1)).registerVendor(request);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void registerGuideCallsService() {
        GuideRegisterRequest request = GuideRegisterRequest.builder()
                .username("guide1")
                .email("guide1@mail.com")
                .password("secret")
                .fullName("Guide One")
                .build();
        MockMultipartFile cv = new MockMultipartFile("cv", "guide-cv.pdf", "application/pdf", "dummy-cv".getBytes());

        ResponseEntity<?> response = authController.registerGuide(request, cv);

        verify(authService, times(1)).registerGuide(request, cv);
        assertEquals(200, response.getStatusCode().value());
    }
}
