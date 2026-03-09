package com.example.manud_jaya.service;

import com.example.manud_jaya.configuration.security.JwtService;
import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.LoginRequest;
import com.example.manud_jaya.model.inbound.request.UserRegisterRequest;
import com.example.manud_jaya.model.inbound.request.VendorRegisterRequest;
import com.example.manud_jaya.model.inbound.response.LoginResponse;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole()
        );
    }

    // REGISTER USER
    public void registerUser(UserRegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // REGISTER VENDOR
    public void registerVendor(VendorRegisterRequest request) {

        VendorProfile vendorProfile = VendorProfile.builder()
                .vendorName(request.getBusinessName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .address(request.getAddress())
                .ktpNumber(request.getKtpNumber())
                .approvalStatus("PENDING")
                .build();


        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VENDOR");
        user.setStatus("PENDING_APPROVAL");
        user.setCreatedAt(LocalDateTime.now());
        user.setVendorProfile(vendorProfile);
        userRepository.save(user);
    }
}
