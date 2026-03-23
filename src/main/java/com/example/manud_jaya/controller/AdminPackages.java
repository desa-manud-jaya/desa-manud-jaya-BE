package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.VendorPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/packages")
public class AdminPackages {

    private final VendorPackageService service;

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        service.approve(id);
        return ResponseEntity.ok("Approved");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable String id,
            @RequestParam String reason
    ) {
        service.reject(id, reason);
        return ResponseEntity.ok("Rejected");
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending() {
        return ResponseEntity.ok(service.getPending());
    }
}
