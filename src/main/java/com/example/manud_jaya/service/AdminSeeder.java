package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = User.builder()
                    .username("admin")
                    .email("admin@travel.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
        }
    }
}