package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void initShouldCreateAdminWhenMissing() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-admin");

        adminSeeder.init();

        verify(userRepository).save(argThat((User u) ->
                "admin".equals(u.getUsername())
                        && "admin@travel.com".equals(u.getEmail())
                        && "encoded-admin".equals(u.getPassword())
                        && "ADMIN".equals(u.getRole())
                        && "ACTIVE".equals(u.getStatus())
                        && u.getCreatedAt() != null
        ));
    }

    @Test
    void initShouldSkipWhenAdminExists() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("a1").build()));

        adminSeeder.init();

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
