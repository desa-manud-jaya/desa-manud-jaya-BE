package com.example.manud_jaya.service;

import com.example.manud_jaya.configuration.security.JwtService;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .id("u-1")
                .username("testuser")
                .password("encoded")
                .role("USER")
                .status("ACTIVE")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token-1");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("u-1", response.getId());
        assertEquals("token-1", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());
    }

    @Test
    void loginUserNotFoundThrows() {
        LoginRequest request = new LoginRequest("unknown", "password");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void loginInvalidPasswordThrows() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = User.builder()
                .username("testuser")
                .password("encoded")
                .status("ACTIVE")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void registerUserSavesRoleAndStatus() {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "new@user.com", "secret");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        authService.registerUser(request);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser")
                        && user.getRole().equals("USER")
                        && user.getStatus().equals("ACTIVE")
                        && user.getPassword().equals("encoded-secret")
        ));
    }

    @Test
    void registerVendorStartsWithPleaseFillProfile() {
        VendorRegisterRequest request = new VendorRegisterRequest();
        request.setUsername("vendor1");
        request.setEmail("vendor1@mail.com");
        request.setPassword("secret");

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        authService.registerVendor(request);

        verify(userRepository).save(argThat(user ->
                user.getRole().equals("VENDOR")
                        && user.getStatus().equals("PLEASE_FILL_PROFILE")
                        && user.getVendorProfile() != null
                        && user.getVendorProfile().getApprovalStatus().equals("PLEASE_FILL_PROFILE")
        ));
    }
}
