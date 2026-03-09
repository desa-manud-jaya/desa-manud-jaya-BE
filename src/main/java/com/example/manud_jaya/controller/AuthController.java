package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        // biasanya cek ke database
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
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
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {

        authService.registerUser(request);

        return ResponseEntity.ok("User registered");
    }

    // VENDOR REGISTER
    @PostMapping("/register/vendor")
    public ResponseEntity<?> registerVendor(@RequestBody VendorRegisterRequest request) {

        authService.registerVendor(request);

        return ResponseEntity.ok("Vendor registered, waiting admin approval");
    }
}
