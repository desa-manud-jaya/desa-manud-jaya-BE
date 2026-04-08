package com.example.manud_jaya.service;

import com.example.manud_jaya.configuration.security.JwtService;
import com.example.manud_jaya.exception.ConflictException;
import com.example.manud_jaya.exception.UnauthorizedException;
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
    void loginUserNotFoundThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("unknown", "password");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void loginRejectedUserThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder()
                .username("testuser")
                .password("encoded")
                .status("REJECTED")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals("User is rejected", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginInvalidPasswordThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = User.builder()
                .username("testuser")
                .password("encoded")
                .status("ACTIVE")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded")).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(request));

        assertEquals("Invalid password", exception.getMessage());
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

    @Test
    void registerUserDuplicateUsernameThrowsConflict() {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "new@user.com", "secret");
        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserDuplicateEmailThrowsConflict() {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "new@user.com", "secret");
        when(userRepository.existsByEmailIgnoreCase("new@user.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerUser(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerVendorDuplicateUsernameThrowsConflict() {
        VendorRegisterRequest request = new VendorRegisterRequest();
        request.setUsername("vendor1");
        request.setEmail("vendor1@mail.com");
        request.setPassword("secret");

        when(userRepository.existsByUsernameIgnoreCase("vendor1")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerVendor(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerVendorDuplicateEmailThrowsConflict() {
        VendorRegisterRequest request = new VendorRegisterRequest();
        request.setUsername("vendor1");
        request.setEmail("vendor1@mail.com");
        request.setPassword("secret");

        when(userRepository.existsByEmailIgnoreCase("vendor1@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.registerVendor(request));

        verify(userRepository, never()).save(any());
    }
}
