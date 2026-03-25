package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreateDestinationRequest;
import com.example.manud_jaya.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/vendor/businesses/{businessId}/destinations")
@RequiredArgsConstructor
@Tag(name = "Vendor Destinations", description = "Vendor endpoints for destination submission and listing")
@SecurityRequirement(name = "bearer-jwt")
public class VendorDestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @Operation(summary = "Create destination", description = "Create destination under owned business. Destination starts with PENDING status.")
    public ResponseEntity<?> createDestination(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            @ModelAttribute CreateDestinationRequest request,
            @RequestParam List<MultipartFile> images,
            Authentication authentication
    ) throws IOException {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.createDestination(
                        businessId,
                        request,
                        images,
                        username
                )
        );
    }

    @GetMapping
    @Operation(summary = "Get all destinations for vendor business")
    public ResponseEntity<?> getDestinations(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.getBusinessDestinations(
                        businessId,
                        username
                )
        );
    }

    @GetMapping("/approved")
    @Operation(summary = "Get approved destinations for vendor business")
    public ResponseEntity<?> getApprovedDestinations(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.getApprovedDestinations(businessId, username)
        );
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending destinations for vendor business")
    public ResponseEntity<?> getPendingDestinations(
            @Parameter(description = "Business ID") @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.getPendingDestinations(businessId, username)
        );
    }
}
