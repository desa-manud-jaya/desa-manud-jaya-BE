package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.DashboardAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
@Tag(name = "Admin Analytics", description = "Admin dashboard analytics endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminAnalyticsController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    @GetMapping("/analytics")
    @Operation(summary = "Get admin dashboard analytics")
    public ResponseEntity<?> getDashboardAnalytics() {
        return ResponseEntity.ok(dashboardAnalyticsService.getAdminAnalytics());
    }
}
