package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VendorOperationsService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PackageRepository packageRepository;
    private final BookingRepository bookingRepository;
    private final DestinationRepository destinationRepository;
    private final ReviewRepository reviewRepository;

    public PagedResponse<Booking> getVendorBookings(String username, int page, int size) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<Business> businesses = businessRepository.findByVendorId(vendor.getId());
        if (businesses.isEmpty()) {
            return PagedResponse.<Booking>builder().items(Collections.emptyList()).page(page).size(size).total(0).build();
        }

        List<String> businessIds = businesses.stream().map(Business::getId).toList();
        Set<String> packageIds = packageRepository.findByBusinessIdIn(businessIds)
                .stream().map(Package::getId).collect(Collectors.toSet());

        List<Booking> bookings = packageIds.isEmpty()
                ? Collections.emptyList()
                : bookingRepository.findByTripIdIn(packageIds.stream().toList());

        int from = Math.min(page * size, bookings.size());
        int to = Math.min(from + size, bookings.size());

        return PagedResponse.<Booking>builder()
                .items(bookings.subList(from, to))
                .page(page)
                .size(size)
                .total(bookings.size())
                .build();
    }

    public PagedResponse<Review> getVendorReviews(String username, int page, int size) {
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
}
