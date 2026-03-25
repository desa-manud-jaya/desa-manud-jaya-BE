package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.VendorPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/packages")
@Tag(name = "Admin Packages", description = "Admin endpoints for package moderation")
@SecurityRequirement(name = "bearer-jwt")
public class AdminPackages {

    private final VendorPackageService service;

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve package", description = "Approve a pending package.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package approved"),
            @ApiResponse(responseCode = "404", description = "Package not found")
    })
    public ResponseEntity<?> approve(@Parameter(description = "Package ID") @PathVariable String id) {
        service.approve(id);
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
            @Parameter(description = "Mandatory rejection reason") @RequestParam String reason
    ) {
        service.reject(id, reason);
        return ResponseEntity.ok("Rejected");
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending packages", description = "Get all pending packages awaiting admin moderation.")
    @ApiResponse(responseCode = "200", description = "Pending packages retrieved")
    public ResponseEntity<?> pending() {
        return ResponseEntity.ok(service.getPending());
    }
}
