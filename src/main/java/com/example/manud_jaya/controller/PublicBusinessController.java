package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.PublicBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
@Tag(name = "Public Businesses", description = "Public endpoints for approved businesses")
public class PublicBusinessController {

    private final PublicBusinessService businessService;

    @GetMapping
    @Operation(summary = "Get approved businesses", description = "Get paginated list of approved businesses.")
    public ResponseEntity<?> getBusinesses(
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                businessService.getApprovedBusinesses(page, size)
        );
    }

    @GetMapping("/{businessId}")
    @Operation(summary = "Get approved business detail")
    public ResponseEntity<?> getBusinessDetail(
            @Parameter(description = "Business ID") @PathVariable String businessId
    ) {

        return ResponseEntity.ok(
                businessService.getBusinessDetail(businessId)
        );
    }
}
