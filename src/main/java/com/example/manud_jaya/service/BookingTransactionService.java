package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.UnauthorizedException;
import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.entity.BookingTransaction;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreateBookingRequest;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.model.inbound.response.RevenueSummaryResponse;
import com.example.manud_jaya.repository.BookingTransactionRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private final BookingTransactionRepository bookingTransactionRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PackageRepository packageRepository;

    public BookingTransaction createBooking(String username, CreateBookingRequest request) {
        validateCreateRequest(request);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        if (!business.getId().equals(pkg.getBusinessId())) {
            throw new ValidationException("Package does not belong to the selected business");
        }

        double price = pkg.getPrice() == null ? 0.0 : pkg.getPrice().doubleValue();
        double totalAmount = price * request.getQuantity();

        BookingTransaction transaction = BookingTransaction.builder()
                .userId(user.getId())
                .businessId(business.getId())
                .packageId(pkg.getId())
                .quantity(request.getQuantity())
                .amount(totalAmount)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .user(user)
                .business(business)
                .build();

        return bookingTransactionRepository.save(transaction);
    }

    public PagedResponse<BookingTransaction> getUserBookingHistory(String username, String userId, int page, int size) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You can only access your own booking history");
        }

        var data = bookingTransactionRepository.findByUserId(userId, PageRequest.of(page, size));
        return PagedResponse.<BookingTransaction>builder()
                .items(data.getContent())
                .page(page)
                .size(size)
                .total(data.getTotalElements())
                .build();
    }

    public RevenueSummaryResponse getRevenueSummary(String startDate, String endDate) {
        List<BookingTransaction> transactions;

        if ((startDate == null || startDate.isBlank()) && (endDate == null || endDate.isBlank())) {
            transactions = bookingTransactionRepository.findAll();
        } else {
            if (startDate == null || startDate.isBlank() || endDate == null || endDate.isBlank()) {
                throw new ValidationException("startDate and endDate must be provided together");
            }

            LocalDate parsedStart;
            LocalDate parsedEnd;
            try {
                parsedStart = LocalDate.parse(startDate);
                parsedEnd = LocalDate.parse(endDate);
            } catch (DateTimeParseException ex) {
                throw new ValidationException("Invalid date format. Expected yyyy-MM-dd", ex);
            }

            if (parsedEnd.isBefore(parsedStart)) {
                throw new ValidationException("endDate must be greater than or equal to startDate");
            }

            transactions = bookingTransactionRepository.findByCreatedAtBetween(
                    parsedStart.atStartOfDay(),
                    parsedEnd.atTime(LocalTime.MAX)
            );
        }

        double totalRevenue = transactions.stream()
                .mapToDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                .sum();

        return RevenueSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .transactionCount(transactions.size())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private void validateCreateRequest(CreateBookingRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }

        if (request.getBusinessId() == null || request.getBusinessId().isBlank()) {
            throw new ValidationException("businessId is required");
        }

        if (request.getPackageId() == null || request.getPackageId().isBlank()) {
            throw new ValidationException("packageId is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than 0");
        }
    }
}
