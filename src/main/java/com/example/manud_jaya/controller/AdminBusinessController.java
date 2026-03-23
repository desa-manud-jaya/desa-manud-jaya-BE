package com.example.manud_jaya.controller;


import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.service.VendorPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/businesses")
@RequiredArgsConstructor
public class AdminBusinessController {

    private final VendorPackageService service;


    @GetMapping("/{businessId}/packages")
    public ResponseEntity<?> getPackagesByBusiness(
            @PathVariable String businessId,
            @RequestParam(required = false) ApprovalStatus status
    ) {
        if (status != null) {
            return ResponseEntity.ok(
                    service.getPackageByStatus(businessId, status)
            );
        }

        return ResponseEntity.ok(
                service.getByBusiness(businessId)
        );
    }
}
