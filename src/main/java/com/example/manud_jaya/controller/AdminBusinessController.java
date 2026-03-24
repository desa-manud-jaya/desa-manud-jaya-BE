package com.example.manud_jaya.controller;


import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.service.VendorPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/businesses")
@RequiredArgsConstructor
@Tag(name = "Admin Business Packages", description = "Admin endpoints to inspect packages by business and status")
@SecurityRequirement(name = "bearer-jwt")
public class AdminBusinessController {

    private final VendorPackageService service;


    @GetMapping("/{businessId}/packages")
    @Operation(summary = "Get packages by business", description = "Get all packages for a business, optionally filtered by approval status.")
    public ResponseEntity<?> getPackagesByBusiness(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            @Parameter(description = "Optional approval status filter") @RequestParam(required = false) ApprovalStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    service.getPackageByStatus(businessId, status)
            );
        }

        return ResponseEntity.ok(
                service.getByBusiness(businessId)
        );
    }
}
