package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.AdminDestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/destinations")
@RequiredArgsConstructor
@Tag(name = "Admin Destinations", description = "Admin destination moderation endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminDestinationController {

    private final AdminDestinationService adminDestinationService;

    @GetMapping("/pending")
    @Operation(summary = "Get pending destinations")
    public ResponseEntity<?> getPendingDestinations() {

        return ResponseEntity.ok(
                adminDestinationService.getPendingDestinations()
        );
    }

    @PutMapping("/{destinationId}/approve")
    @Operation(summary = "Approve destination")
    public ResponseEntity<?> approveDestination(
            @Parameter(description = "Destination ID") @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                adminDestinationService.approveDestination(destinationId)
        );
    }

    @PutMapping("/{destinationId}/reject")
    @Operation(summary = "Reject destination")
    public ResponseEntity<?> rejectDestination(
            @Parameter(description = "Destination ID") @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                adminDestinationService.rejectDestination(destinationId)
        );
    }
}
