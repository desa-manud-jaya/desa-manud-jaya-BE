package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.AdminBookingDecisionRequest;
import com.example.manud_jaya.service.AdminOperationsService;
import com.example.manud_jaya.service.BookingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Operations", description = "Admin booking and review listing endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;
    private final BookingTransactionService bookingTransactionService;

    @GetMapping("/bookings")
    @Operation(summary = "Get bookings")
    public ResponseEntity<?> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminOperationsService.getBookings(page, size));
    }

    @GetMapping("/bookings/payment")
    @Operation(summary = "Get booking payments", description = "Get booking payment transactions with optional status filter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking payment list fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters")
    })
    public ResponseEntity<?> getBookingPayments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingTransactionService.getAdminBookingPayments(status, page, size));
    }

    @GetMapping("/bookings/payment/{bookingId}")
    @Operation(summary = "Get booking payment detail", description = "Get booking payment detail for admin review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking payment detail fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<?> getBookingPaymentDetail(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingTransactionService.getAdminBookingPaymentDetail(bookingId));
    }

    @PatchMapping("/bookings/payment/{bookingId}/decision")
    @Operation(summary = "Review booking payment", description = "Approve or reject a pending booking payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking payment reviewed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid decision or booking status"),
            @ApiResponse(responseCode = "404", description = "Admin or booking not found")
    })
    public ResponseEntity<?> reviewBookingPayment(
            Authentication authentication,
            @PathVariable String bookingId,
            @RequestBody AdminBookingDecisionRequest request
    ) {
        return ResponseEntity.ok(
                bookingTransactionService.reviewBookingPayment(
                        authentication.getName(),
                        bookingId,
                        request.getDecision(),
                        request.getNote()
                )
        );
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get reviews")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminOperationsService.getReviews(page, size));
    }
}
