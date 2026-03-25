package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.AdminOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Operations", description = "Admin booking and review listing endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;

    @GetMapping("/bookings")
    @Operation(summary = "Get bookings")
    public ResponseEntity<?> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminOperationsService.getBookings(page, size));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get reviews")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminOperationsService.getReviews(page, size));
    }
}
