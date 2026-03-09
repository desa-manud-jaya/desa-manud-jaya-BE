package com.example.manud_jaya.controller;


import com.example.manud_jaya.model.inbound.response.VendorPendingResponse;
import com.example.manud_jaya.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // GET pending vendors
    @GetMapping("/vendors/pending")
    public ResponseEntity<List<VendorPendingResponse>> getPendingVendors() {

        return ResponseEntity.ok(adminService.getPendingVendors());
    }

    // APPROVE vendor
    @PostMapping("/vendors/{userId}/approve")
    public ResponseEntity<?> approveVendor(
            @PathVariable String userId,
            Authentication authentication
    ) {

        String adminUsername = authentication.getName();

        adminService.approveVendor(userId, adminUsername);

        return ResponseEntity.ok("Vendor approved");
    }
}
