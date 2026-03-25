package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.VendorOperationsService;
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
@RequestMapping("/vendor")
@Tag(name = "Vendor Operations", description = "Vendor booking and review management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class VendorOperationsController {

    private final VendorOperationsService vendorOperationsService;

    @GetMapping("/bookings")
    @Operation(summary = "Get vendor bookings")
    public ResponseEntity<?> getBookings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(vendorOperationsService.getVendorBookings(authentication.getName(), page, size));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get vendor reviews")
    public ResponseEntity<?> getReviews(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(vendorOperationsService.getVendorReviews(authentication.getName(), page, size));
    }
}
