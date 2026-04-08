package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.DashboardAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vendor")
@Tag(name = "Vendor Analytics", description = "Vendor dashboard analytics and impact metrics")
@SecurityRequirement(name = "bearer-jwt")
public class VendorAnalyticsController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    @GetMapping("/dashboard/analytics")
    @Operation(summary = "Get partner dashboard analytics")
    public ResponseEntity<?> getDashboardAnalytics(Authentication authentication) {
        return ResponseEntity.ok(dashboardAnalyticsService.getPartnerAnalytics(authentication.getName()));
    }

    @GetMapping("/impact-analytics")
    @Operation(summary = "Get partner impact analytics")
    public ResponseEntity<?> getImpactAnalytics(Authentication authentication) {
        return ResponseEntity.ok(dashboardAnalyticsService.getVendorImpactAnalytics(authentication.getName()));
    }
}
