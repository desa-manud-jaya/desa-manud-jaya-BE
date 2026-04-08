package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.entity.BookingTransaction;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.repository.BookingTransactionRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorOperationsService {

    private static final String STATUS_APPROVED = "approved";

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final BookingTransactionRepository bookingTransactionRepository;
    private final DestinationRepository destinationRepository;
    private final ReviewRepository reviewRepository;

    public PagedResponse<BookingTransaction> getVendorBookings(String username, int page, int size) {
        validatePagination(page, size);

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<String> businessIds = businessRepository.findByVendorId(vendor.getId())
                .stream()
                .map(Business::getId)
                .toList();

        if (businessIds.isEmpty()) {
            return PagedResponse.<BookingTransaction>builder().items(Collections.emptyList()).page(page).size(size).total(0).build();
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

    public PagedResponse<Review> getVendorReviews(String username, int page, int size) {
        validatePagination(page, size);

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<Business> businesses = businessRepository.findByVendorId(vendor.getId());
        if (businesses.isEmpty()) {
            return PagedResponse.<Review>builder().items(Collections.emptyList()).page(page).size(size).total(0).build();
        }

        List<String> businessIds = businesses.stream().map(Business::getId).toList();
        Set<String> destinationIds = businessIds.stream()
                .flatMap(id -> destinationRepository.findByBusinessId(id).stream())
                .map(Destination::getId)
                .collect(Collectors.toSet());

        List<Review> reviews = destinationIds.isEmpty()
                ? Collections.emptyList()
                : reviewRepository.findByDestinationIdIn(destinationIds.stream().toList());

        int from = Math.min(page * size, reviews.size());
        int to = Math.min(from + size, reviews.size());

        return PagedResponse.<Review>builder()
                .items(reviews.subList(from, to))
                .page(page)
                .size(size)
                .total(reviews.size())
                .build();
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
