package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.model.inbound.request.CreateDestinationRequest;
import com.example.manud_jaya.service.DestinationService;
import com.example.manud_jaya.service.VendorService;
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
public class VendorDestinationController {

    private final DestinationService destinationService;

    @PostMapping
    public ResponseEntity<?> createDestination(
            @PathVariable String businessId,
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
    public ResponseEntity<?> getDestinations(
            @PathVariable String businessId,
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
    public ResponseEntity<?> getApprovedDestinations(
            @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.getApprovedDestinations(businessId, username)
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDestinations(
            @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                destinationService.getPendingDestinations(businessId, username)
        );
    }
}
