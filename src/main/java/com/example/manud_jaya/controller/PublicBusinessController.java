package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.response.VendorBusinessDetailResponse;
import com.example.manud_jaya.model.inbound.response.VendorBusinessResponse;
import com.example.manud_jaya.service.PublicBusinessService;
import com.example.manud_jaya.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class PublicBusinessController {

    private final PublicBusinessService businessService;

    @GetMapping
    public ResponseEntity<?> getBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                businessService.getApprovedBusinesses(page, size)
        );
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<?> getBusinessDetail(
            @PathVariable String businessId
    ) {

        return ResponseEntity.ok(
                businessService.getBusinessDetail(businessId)
        );
    }
}
