package com.example.manud_jaya.controller;


import com.example.manud_jaya.model.inbound.response.GuidePendingResponse;
import com.example.manud_jaya.model.inbound.response.VendorPendingResponse;
import com.example.manud_jaya.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Vendors", description = "Admin endpoints for vendor approval workflow")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/vendors/pending")
    @Operation(summary = "Get pending vendors")
    @ApiResponse(responseCode = "200", description = "Pending vendors retrieved")
    public ResponseEntity<List<VendorPendingResponse>> getPendingVendors() {

        return ResponseEntity.ok(adminService.getPendingVendors());
    }

    @GetMapping("/vendors/approved")
    @Operation(summary = "Get approved vendors")
    @ApiResponse(responseCode = "200", description = "Approved vendors retrieved")
    public ResponseEntity<List<VendorPendingResponse>> getApprovedVendors() {

        return ResponseEntity.ok(adminService.getApproveVendors());
    }

    @PostMapping("/vendors/{userId}/approve")
    @Operation(summary = "Approve vendor", description = "Approve a vendor and auto-create approved business (1:1 relation).")
    @ApiResponse(responseCode = "200", description = "Vendor approved")
    public ResponseEntity<?> approveVendor(
            @Parameter(description = "Vendor user ID") @PathVariable String userId,
            Authentication authentication
    ) {

        String adminUsername = authentication.getName();

        adminService.approveVendor(userId, adminUsername);

        return ResponseEntity.ok("Vendor approved");
    }

    @PostMapping("/vendors/{userId}/reject")
    @Operation(summary = "Reject vendor")
    @ApiResponse(responseCode = "200", description = "Vendor rejected")
    public ResponseEntity<?> rejectVendor(
            @Parameter(description = "Vendor user ID") @PathVariable String userId,
            Authentication authentication
    ) {

        String adminUsername = authentication.getName();

        adminService.rejectVendor(userId, adminUsername);

        return ResponseEntity.ok("Vendor Rejected");
    }

    @GetMapping("/guides/pending")
    @Operation(summary = "Get pending guides")
    @ApiResponse(responseCode = "200", description = "Pending guides retrieved")
    public ResponseEntity<List<GuidePendingResponse>> getPendingGuides() {
        return ResponseEntity.ok(adminService.getPendingGuides());
    }

    @GetMapping("/guides/approved")
    @Operation(summary = "Get approved guides", description = "Get approved guides. Optional tripDate (yyyy-MM-dd) filters only guides available on that date.")
    @ApiResponse(responseCode = "200", description = "Approved guides retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid tripDate format")
    public ResponseEntity<List<GuidePendingResponse>> getApprovedGuides(
            @RequestParam(required = false) String tripDate
    ) {
        return ResponseEntity.ok(adminService.getApprovedGuides(tripDate));
    }

    @PostMapping("/guides/{userId}/approve")
    @Operation(summary = "Approve guide")
    @ApiResponse(responseCode = "200", description = "Guide approved")
    public ResponseEntity<?> approveGuide(
            @Parameter(description = "Guide user ID") @PathVariable String userId,
            Authentication authentication
    ) {
        String adminUsername = authentication.getName();
        adminService.approveGuide(userId, adminUsername);
        return ResponseEntity.ok("Guide approved");
    }

    @PostMapping("/guides/{userId}/reject")
    @Operation(summary = "Reject guide")
    @ApiResponse(responseCode = "200", description = "Guide rejected")
    public ResponseEntity<?> rejectGuide(
            @Parameter(description = "Guide user ID") @PathVariable String userId,
            @RequestParam String reason,
            Authentication authentication
    ) {
        String adminUsername = authentication.getName();
        adminService.rejectGuide(userId, adminUsername, reason);
        return ResponseEntity.ok("Guide rejected");
    }
}
