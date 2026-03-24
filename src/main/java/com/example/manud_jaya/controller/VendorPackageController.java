package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.service.SupabaseStorageService;
import com.example.manud_jaya.service.VendorPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/vendor/businesses/{businessId}/packages")
@RequiredArgsConstructor
@Tag(name = "Vendor Packages", description = "Vendor endpoints for package submission and package listing")
@SecurityRequirement(name = "bearer-jwt")
public class VendorPackageController {
    private final VendorPackageService vendorPackageService;
    private final SupabaseStorageService supabaseStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create package", description = "Create a new package under a vendor-owned business. Newly created package is always PENDING.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package submitted successfully"),
            @ApiResponse(responseCode = "403", description = "Vendor does not own the business"),
            @ApiResponse(responseCode = "404", description = "Vendor or business not found")
    })
    public ResponseEntity<?> createPackage(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            @RequestPart("data") CreatePackageRequest request,
            @RequestPart("requirementDocument") MultipartFile doc,
            @RequestPart("photo") MultipartFile photo,
            Authentication authentication
    ) throws IOException {

        String username = authentication.getName();

        String docUrl = supabaseStorageService.uploadDocument(doc);
        String photoUrl = supabaseStorageService.uploadImage(photo);

        vendorPackageService.createPackage(request, businessId, username, docUrl, photoUrl);

        return ResponseEntity.ok("Package submitted (PENDING)");
    }

    @GetMapping
    @Operation(summary = "Get vendor packages by business", description = "Get all packages in a business owned by the authenticated vendor, including rejection reason if status is REJECTED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Vendor does not own the business")
    })
    public ResponseEntity<?> getPackages(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            Authentication authentication
    ) {
        String username = authentication.getName();

        return ResponseEntity.ok(vendorPackageService.getByBusinessForVendor(businessId, username));
    }

}
