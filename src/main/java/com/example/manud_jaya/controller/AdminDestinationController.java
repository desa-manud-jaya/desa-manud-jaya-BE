package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.AdminDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/destinations")
@RequiredArgsConstructor
public class AdminDestinationController {

    private final AdminDestinationService adminDestinationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDestinations() {

        return ResponseEntity.ok(
                adminDestinationService.getPendingDestinations()
        );
    }

    @PutMapping("/{destinationId}/approve")
    public ResponseEntity<?> approveDestination(
            @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                adminDestinationService.approveDestination(destinationId)
        );
    }

    @PutMapping("/{destinationId}/reject")
    public ResponseEntity<?> rejectDestination(
            @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                adminDestinationService.rejectDestination(destinationId)
        );
    }
}
