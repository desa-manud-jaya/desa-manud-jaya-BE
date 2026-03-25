package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.PublicDestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Public Destinations", description = "Public endpoints for destinations")
public class PublicDestinationController {

    private final PublicDestinationService destinationService;

    @GetMapping("/destinations/approved")
    @Operation(summary = "Get all approved destinations", description = "Get all destinations with APPROVED status.")
    @ApiResponse(responseCode = "200", description = "Approved destinations retrieved")
    public ResponseEntity<?> getAllApprovedDestinations() {
        return ResponseEntity.ok(destinationService.getAllApprovedDestinations());
    }

    @GetMapping("/businesses/{businessId}/destinations")
    @Operation(summary = "Get destinations by business")
    public ResponseEntity<?> getBusinessDestinations(
            @Parameter(description = "Business ID") @PathVariable String businessId
    ) {

        return ResponseEntity.ok(
                destinationService.getBusinessDestinations(businessId)
        );
    }

    @GetMapping("/destinations/{destinationId}")
    @Operation(summary = "Get destination detail")
    public ResponseEntity<?> getDestinationDetail(
            @Parameter(description = "Destination ID") @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                destinationService.getDestinationDetail(destinationId)
        );
    }
}
