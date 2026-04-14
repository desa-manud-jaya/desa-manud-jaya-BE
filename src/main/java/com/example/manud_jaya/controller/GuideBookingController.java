package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.BookingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guide")
@Tag(name = "Guide Bookings", description = "Guide booking management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class GuideBookingController {

    private final BookingTransactionService bookingTransactionService;

    @GetMapping("/bookings")
    @Operation(summary = "Get guide bookings", description = "Get bookings assigned to authenticated guide.")
    public ResponseEntity<?> getGuideBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingTransactionService.getGuideBookings(authentication.getName(), page, size));
    }
}
