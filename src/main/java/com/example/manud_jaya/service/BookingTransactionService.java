package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ConflictException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private static final String STATUS_WAITING_FOR_PAYMENT = "waiting_for_payment";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";

    private final BookingTransactionRepository bookingTransactionRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PackageRepository packageRepository;
    private final SupabaseStorageService supabaseStorageService;

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

        if (pkg.getGuideId() == null || pkg.getGuideId().isBlank()) {
            throw new ValidationException("Selected package does not have assigned guide");
        }

        LocalDate tripDate = parseTripDate(request.getTripDate());

        boolean occupied = bookingTransactionRepository.existsByGuideIdAndTripDateAndStatusIn(
                pkg.getGuideId(),
                tripDate,
                List.of(STATUS_WAITING_FOR_PAYMENT, STATUS_PENDING, STATUS_APPROVED)
        );

        if (occupied) {
            throw new ConflictException("Guide is already assigned on the selected tripDate");
        }

        double price = pkg.getPrice() == null ? 0.0 : pkg.getPrice().doubleValue();
        double totalAmount = price * request.getQuantity();

        BookingTransaction transaction = BookingTransaction.builder()
                .userId(user.getId())
                .businessId(business.getId())
                .packageId(pkg.getId())
                .guideId(pkg.getGuideId())
                .tripDate(tripDate)
                .quantity(request.getQuantity())
                .amount(totalAmount)
                .status(STATUS_WAITING_FOR_PAYMENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .business(business)
                .build();

        return bookingTransactionRepository.save(transaction);
    }

    public BookingTransaction uploadPaymentProof(String username, String bookingId, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BookingTransaction transaction = bookingTransactionRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!user.getId().equals(transaction.getUserId())) {
            throw new UnauthorizedException("You can only upload payment proof for your own booking");
        }

        if (!STATUS_WAITING_FOR_PAYMENT.equals(transaction.getStatus())) {
            throw new ValidationException("Payment proof can only be uploaded when booking status is waiting_for_payment");
        }

        try {
            String proofUrl = supabaseStorageService.uploadDocument(file);
            transaction.setPaymentProofUrl(proofUrl);
            transaction.setPaymentUploadedAt(LocalDateTime.now());
            transaction.setStatus(STATUS_PENDING);
            transaction.setUpdatedAt(LocalDateTime.now());
            return bookingTransactionRepository.save(transaction);
        } catch (IOException ex) {
            throw new ValidationException("Failed to upload payment proof", ex);
        }
    }

    public PagedResponse<BookingTransaction> getUserBookingHistory(String username, String userId, int page, int size) {
        validatePagination(page, size);

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

    public PagedResponse<BookingTransaction> getAdminBookingPayments(String status, int page, int size) {
        validatePagination(page, size);

        var pageable = PageRequest.of(page, size);
        var data = (status == null || status.isBlank())
                ? bookingTransactionRepository.findAll(pageable)
                : bookingTransactionRepository.findByStatus(status.trim().toLowerCase(), pageable);

        return PagedResponse.<BookingTransaction>builder()
                .items(data.getContent())
                .page(page)
                .size(size)
                .total(data.getTotalElements())
                .build();
    }

    public PagedResponse<BookingTransaction> getGuideBookings(String username, int page, int size) {
        validatePagination(page, size);

        User guide = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        if (!"GUIDE".equalsIgnoreCase(guide.getRole())) {
            throw new UnauthorizedException("Only guide account can access guide bookings");
        }

        var data = bookingTransactionRepository.findByGuideId(guide.getId(), PageRequest.of(page, size));

        return PagedResponse.<BookingTransaction>builder()
                .items(data.getContent())
                .page(page)
                .size(size)
                .total(data.getTotalElements())
                .build();
    }

    public BookingTransaction getAdminBookingPaymentDetail(String bookingId) {
        return bookingTransactionRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public BookingTransaction reviewBookingPayment(String adminUsername, String bookingId, String decision, String note) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        BookingTransaction transaction = bookingTransactionRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!STATUS_PENDING.equals(transaction.getStatus())) {
            throw new ValidationException("Only pending booking payments can be reviewed");
        }

        if (decision == null || decision.isBlank()) {
            throw new ValidationException("decision is required");
        }

        String normalizedDecision = decision.trim().toUpperCase();
        if ("APPROVE".equals(normalizedDecision)) {
            transaction.setStatus(STATUS_APPROVED);
        } else if ("REJECT".equals(normalizedDecision)) {
            if (note == null || note.isBlank()) {
                throw new ValidationException("note is required for reject decision");
            }
            transaction.setStatus(STATUS_REJECTED);
        } else {
            throw new ValidationException("decision must be APPROVE or REJECT");
        }

        transaction.setReviewedAt(LocalDateTime.now());
        transaction.setReviewedBy(admin.getId());
        transaction.setReviewNote(note);
        transaction.setUpdatedAt(LocalDateTime.now());

        return bookingTransactionRepository.save(transaction);
    }

    public PagedResponse<BookingTransaction> getVendorApprovedBookings(String username, int page, int size) {
        validatePagination(page, size);

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        List<String> businessIds = businessRepository.findByVendorId(vendor.getId())
                .stream()
                .map(Business::getId)
                .toList();

        if (businessIds.isEmpty()) {
            return PagedResponse.<BookingTransaction>builder()
                    .items(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .total(0)
                    .build();
        }

        var data = bookingTransactionRepository.findByBusinessIdInAndStatus(
                businessIds,
                STATUS_APPROVED,
                PageRequest.of(page, size)
        );

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

        if (request.getTripDate() == null || request.getTripDate().isBlank()) {
            throw new ValidationException("tripDate is required");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than 0");
        }
    }

    private LocalDate parseTripDate(String tripDate) {
        try {
            return LocalDate.parse(tripDate);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Invalid tripDate format. Expected yyyy-MM-dd", ex);
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new ValidationException("size must be greater than 0");
        }
    }
}
