package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.response.VendorBusinessDetailResponse;
import com.example.manud_jaya.model.inbound.response.VendorBusinessResponse;
import com.example.manud_jaya.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final VendorService vendorService;

    @GetMapping("")
    public ResponseEntity<List<VendorBusinessResponse>> getApprovedBusinesses() {

        return ResponseEntity.ok(vendorService.getApprovedBusinesses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorBusinessDetailResponse> getBusinessById(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(vendorService.getBusinessById(id));
    }
}
