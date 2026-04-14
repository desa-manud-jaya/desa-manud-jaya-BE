package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.service.AuthService;
import com.example.manud_jaya.service.VendorPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/packages")
@Tag(name = "Admin Packages", description = "Admin endpoints for package moderation")
@SecurityRequirement(name = "bearer-jwt")
public class AdminPackages {

    private final VendorPackageService service;
    private final AuthService authService;

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve package", description = "Approve a pending package.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package approved"),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    public ResponseEntity<?> approve(
            @Parameter(description = "Package ID") @PathVariable String id,
            Authentication authentication
    ) {
        User admin = authService.getUser(authentication.getName());
        service.approve(id, admin.getId(), null);
        return ResponseEntity.ok("Approved");
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject package", description = "Reject a package. Rejection reason is mandatory and visible to vendor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package rejected"),
            @ApiResponse(responseCode = "400", description = "Missing rejection reason"),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    public ResponseEntity<?> reject(
            @Parameter(description = "Package ID") @PathVariable String id,
            @Parameter(description = "Mandatory rejection reason") @RequestParam String reason,
            Authentication authentication
    ) {
        User admin = authService.getUser(authentication.getName());
        service.reject(id, reason, admin.getId());
        return ResponseEntity.ok("Rejected");
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending packages", description = "Get all pending packages awaiting admin moderation.")
    @ApiResponse(responseCode = "200", description = "Pending packages retrieved")
    public ResponseEntity<?> pending() {
        return ResponseEntity.ok(service.getPending());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package detail for moderation")
    public ResponseEntity<?> detail(@PathVariable String id) {
        return ResponseEntity.ok(service.getPackageDetail(id));
    }

    @PutMapping("/{id}/assign-guide")
    @Operation(summary = "Assign guide to package", description = "Assign an approved guide to selected package.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide assigned"),
            @ApiResponse(responseCode = "400", description = "Guide is not approved or invalid request"),
            @ApiResponse(responseCode = "404", description = "Package or guide not found")
    })
    public ResponseEntity<?> assignGuide(
            @PathVariable String id,
            @RequestParam String guideId
    ) {
        return ResponseEntity.ok(service.assignGuideToPackage(id, guideId));
    }

    @GetMapping("/deletion-requests")
    @Operation(summary = "Get package deletion requests queue")
    public ResponseEntity<?> deletionRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getDeletionRequests(page, size));
    }

    @PutMapping("/{id}/deletion-approve")
    @Operation(summary = "Approve package deletion request")
    public ResponseEntity<?> approveDeletion(
            @PathVariable String id,
            @RequestParam(required = false) String note,
            Authentication authentication
    ) {
        User admin = authService.getUser(authentication.getName());
        return ResponseEntity.ok(service.approveDeletionRequest(id, admin.getId(), note));
    }

    @PutMapping("/{id}/deletion-reject")
    @Operation(summary = "Reject package deletion request")
    public ResponseEntity<?> rejectDeletion(
            @PathVariable String id,
            @RequestParam String reason,
            Authentication authentication
    ) {
        User admin = authService.getUser(authentication.getName());
        return ResponseEntity.ok(service.rejectDeletionRequest(id, admin.getId(), reason));
    }
}
