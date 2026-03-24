package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor/business")
@RequiredArgsConstructor
@Tag(name = "Vendor Business", description = "Vendor endpoints for business ownership")
@SecurityRequirement(name = "bearer-jwt")
public class VendorBusinessController {

    private final VendorService vendorService;

    @PostMapping
    @Operation(summary = "Create business", description = "Create a business for authenticated vendor (only if vendor has no business yet).")
    public ResponseEntity<?> createBusiness(
            @RequestBody CreateBusinessRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                vendorService.createBusiness(request, username)
        );
    }


    @GetMapping("/{businessId}")
    @Operation(summary = "Get vendor business detail")
    public ResponseEntity<?> getBusinessDetail(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                vendorService.getBusinessDetail(businessId, username)
        );
    }
}
