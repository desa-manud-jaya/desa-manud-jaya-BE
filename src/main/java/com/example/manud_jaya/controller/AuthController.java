package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.GuideRegisterRequest;
import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password, then receive JWT token.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Authentication failed: user not found, rejected, or invalid password")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get auth profile", description = "Get current authenticated principal info from JWT.")
    @SecurityRequirement(name = "bearer-jwt")
    public String profile() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username = auth.getName();

        String role = auth.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        return "Username: " + username + " Role: " + role;
    }

    @PostMapping("/register/user")
    @Operation(summary = "Register user", description = "Register a normal USER account.")
    @ApiResponse(responseCode = "200", description = "User registered")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {

        authService.registerUser(request);

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/register/vendor")
    @Operation(summary = "Register vendor", description = "Register a VENDOR account in PLEASE_FILL_PROFILE state.")
    @ApiResponse(responseCode = "200", description = "Vendor registered")
    public ResponseEntity<?> registerVendor(@RequestBody VendorRegisterRequest request) {

        authService.registerVendor(request);

        return ResponseEntity.ok("Vendor registered, waiting admin approval");
    }

    @PostMapping("/register/guide")
    @Operation(summary = "Register guide", description = "Register a GUIDE account in PENDING state for admin verification.")
    @ApiResponse(responseCode = "200", description = "Guide registered")
    public ResponseEntity<?> registerGuide(@RequestBody GuideRegisterRequest request) {

        authService.registerGuide(request);

        return ResponseEntity.ok("Guide registered, waiting admin approval");
    }
}
