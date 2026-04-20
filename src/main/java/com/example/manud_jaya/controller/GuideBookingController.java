package com.example.manud_jaya.controller;

import com.example.manud_jaya.exception.UnauthorizedException;
import com.example.manud_jaya.model.dto.GuideProfile;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.GuideProfileResponse;
import com.example.manud_jaya.service.AuthService;
import com.example.manud_jaya.service.BookingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final AuthService authService;

    @GetMapping("/profile")
    @Operation(summary = "Get guide profile", description = "Get personal profile of currently authenticated guide.")
    @ApiResponse(responseCode = "200", description = "Guide profile retrieved")
    @ApiResponse(responseCode = "401", description = "Only guide account can access this endpoint")
    public ResponseEntity<GuideProfileResponse> getGuideProfile(Authentication authentication) {
        User user = authService.getUser(authentication.getName());

        if (!"GUIDE".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedException("Only guide account can access guide profile");
        }

        GuideProfile profile = user.getGuideProfile();

        GuideProfileResponse response = GuideProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(profile != null ? profile.getFullName() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .licenseNumber(profile != null ? profile.getLicenseNumber() : null)
                .approvalStatus(profile != null ? profile.getApprovalStatus() : null)
                .approvedAt(profile != null ? profile.getApprovedAt() : null)
                .rejectionReason(profile != null ? profile.getRejectionReason() : null)
                .build();

        return ResponseEntity.ok(response);
    }

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
