package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.VendorPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Public Packages", description = "Public endpoints for approved travel packages")
public class PublicPackageController {

    private final VendorPackageService vendorPackageService;

    @GetMapping("/businesses/{businessId}/packages")
    @Operation(summary = "Get approved packages by business", description = "Get only APPROVED packages for a business.")
    @ApiResponse(responseCode = "200", description = "Approved packages retrieved")
    public ResponseEntity<?> getApprovedPackagesByBusiness(
            @Parameter(description = "Business ID") @PathVariable String businessId
    ) {
        return ResponseEntity.ok(vendorPackageService.getApprovedByBusiness(businessId));
    }

    @GetMapping("/packages/{packageId}")
    @Operation(summary = "Get approved package detail", description = "Get package detail only if package status is APPROVED.")
    @ApiResponse(responseCode = "200", description = "Approved package detail retrieved")
    public ResponseEntity<?> getApprovedPackageDetail(
            @Parameter(description = "Package ID") @PathVariable String packageId
    ) {
        return ResponseEntity.ok(vendorPackageService.getApprovedDetail(packageId));
    }
}
