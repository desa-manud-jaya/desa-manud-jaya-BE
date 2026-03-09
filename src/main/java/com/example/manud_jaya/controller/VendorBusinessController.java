package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor/business")
@RequiredArgsConstructor
public class VendorBusinessController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<?> createBusiness(
            @RequestBody CreateBusinessRequest request,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                vendorService.createBusiness(request, username)
        );
    }

    @GetMapping
    public ResponseEntity<?> getMyBusinesses(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                vendorService.getVendorBusinesses(username)
        );
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<?> getBusinessDetail(
            @PathVariable String businessId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                vendorService.getBusinessDetail(businessId, username)
        );
    }
}
