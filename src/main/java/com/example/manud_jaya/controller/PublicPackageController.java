package com.example.manud_jaya.controller;

import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.inbound.response.ApprovedListingsResponse;
import com.example.manud_jaya.service.PublicDestinationService;
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
    private final PublicDestinationService publicDestinationService;

    @GetMapping("/packages/approved")
    @Operation(summary = "Get all approved packages", description = "Get all packages with APPROVED status.")
    @ApiResponse(responseCode = "200", description = "Approved packages retrieved")
    public ResponseEntity<?> getAllApprovedPackages() {
        return ResponseEntity.ok(vendorPackageService.getAllApprovedPackages());
    }

    @GetMapping("/packages/approved/with-destinations")
    @Operation(summary = "Get approved packages and destinations", description = "Get approved packages and destinations in one response. Optional pagination is supported when both page and size are provided.")
    @ApiResponse(responseCode = "200", description = "Approved packages and destinations retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    public ResponseEntity<?> getAllApprovedPackagesAndDestinations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        boolean usePagination = page != null || size != null;

        if ((page == null) != (size == null)) {
            throw new ValidationException("page and size must be provided together");
        }

        var packages = usePagination
                ? vendorPackageService.getAllApprovedPackages(page, size)
                : vendorPackageService.getAllApprovedPackages();

        var destinations = usePagination
                ? publicDestinationService.getAllApprovedDestinations(page, size)
                : publicDestinationService.getAllApprovedDestinations();

        ApprovedListingsResponse response = ApprovedListingsResponse.builder()
                .packages(packages)
                .destinations(destinations)
                .page(usePagination ? page : 0)
                .size(usePagination ? size : packages.size())
                .totalPackages(vendorPackageService.countAllApprovedPackages())
                .totalDestinations(publicDestinationService.countAllApprovedDestinations())
                .build();

        return ResponseEntity.ok(response);
    }

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
