package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.PublicDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicDestinationController {

    private final PublicDestinationService destinationService;

    @GetMapping("/businesses/{businessId}/destinations")
    public ResponseEntity<?> getBusinessDestinations(
            @PathVariable String businessId
    ) {

        return ResponseEntity.ok(
                destinationService.getBusinessDestinations(businessId)
        );
    }

    @GetMapping("/destinations/{destinationId}")
    public ResponseEntity<?> getDestinationDetail(
            @PathVariable String destinationId
    ) {

        return ResponseEntity.ok(
                destinationService.getDestinationDetail(destinationId)
        );
    }
}
