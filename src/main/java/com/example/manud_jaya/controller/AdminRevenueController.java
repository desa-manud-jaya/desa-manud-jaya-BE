package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.BookingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/revenue")
@Tag(name = "Admin Revenue", description = "Admin revenue analytics endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminRevenueController {

    private final BookingTransactionService bookingTransactionService;

    @GetMapping("/summary")
    @Operation(summary = "Get revenue summary", description = "Get total revenue with optional date range filter using yyyy-MM-dd format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Revenue summary fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or invalid date range")
    })
    public ResponseEntity<?> getRevenueSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(bookingTransactionService.getRevenueSummary(startDate, endDate));
    }
}
