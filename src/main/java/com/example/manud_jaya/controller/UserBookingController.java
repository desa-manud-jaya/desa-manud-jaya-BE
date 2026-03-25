package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreateBookingRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/bookings")
@Tag(name = "User Bookings", description = "User booking transaction endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class UserBookingController {

    private final BookingTransactionService bookingTransactionService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Create a new booking transaction with default status pending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Related user, business, or package not found")
    })
    public ResponseEntity<?> createBooking(
            Authentication authentication,
            @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.ok(bookingTransactionService.createBooking(authentication.getName(), request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user booking history", description = "Get booking history by user id for current authenticated user only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking history fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Attempt to access another user booking history")
    })
    public ResponseEntity<?> getBookingHistory(
            Authentication authentication,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingTransactionService.getUserBookingHistory(authentication.getName(), userId, page, size));
    }
}
