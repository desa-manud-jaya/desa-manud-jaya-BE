package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.model.inbound.request.PackageDeletionRequest;
import com.example.manud_jaya.model.inbound.request.UpdatePackageRequest;
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

    @PutMapping("/{packageId}")
    @Operation(summary = "Update package", description = "Update package data for authenticated vendor-owned business.")
    public ResponseEntity<?> updatePackage(
            @PathVariable String businessId,
            @PathVariable String packageId,
            @RequestBody UpdatePackageRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vendorPackageService.updatePackage(
                businessId,
                packageId,
                authentication.getName(),
                request
        ));
    }

    @PostMapping("/{packageId}/deletion-request")
    @Operation(summary = "Request package deletion", description = "Submit package deletion request for admin moderation.")
    public ResponseEntity<?> requestDeletion(
            @PathVariable String businessId,
            @PathVariable String packageId,
            @RequestBody PackageDeletionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vendorPackageService.requestDeletion(
                businessId,
                packageId,
                authentication.getName(),
                request.getReason()
        ));
    }

    @GetMapping
    @Operation(summary = "Get vendor packages by business", description = "Get packages in a business owned by authenticated vendor with optional filter/search/pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Vendor does not own the business")
    })
    public ResponseEntity<?> getPackages(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) DeletionRequestStatus deletionRequestStatus,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String username = authentication.getName();

        if (approvalStatus == null && deletionRequestStatus == null && (q == null || q.isBlank()) && page == 0 && size == 10) {
            return ResponseEntity.ok(vendorPackageService.getByBusinessForVendor(businessId, username));
        }

        return ResponseEntity.ok(vendorPackageService.getByBusinessForVendor(
                businessId,
                username,
                approvalStatus,
                deletionRequestStatus,
                q,
                page,
                size
        ));
    }

    // Backward-compatible helper for existing unit tests and internal calls.
    public ResponseEntity<?> getPackages(String businessId, Authentication authentication) {
        return getPackages(businessId, null, null, null, 0, 10, authentication);
    }

}
