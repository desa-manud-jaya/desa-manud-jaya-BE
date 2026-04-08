package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.AdminDashboardAnalyticsResponse;
import com.example.manud_jaya.model.inbound.response.ImpactAnalyticsResponse;
import com.example.manud_jaya.model.inbound.response.PartnerDashboardAnalyticsResponse;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.PaymentRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PackageRepository packageRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VendorDocumentRepository vendorDocumentRepository;
    private final DestinationRepository destinationRepository;
    private final ReviewRepository reviewRepository;

    public PartnerDashboardAnalyticsResponse getPartnerAnalytics(String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        List<Business> businesses = businessRepository.findByVendorId(vendor.getId());
        if (businesses.isEmpty()) {
            return PartnerDashboardAnalyticsResponse.builder()
                    .totalRevenue(0)
                    .activeListings(0)
                    .totalBookings(0)
                    .pendingPackages(0)
                    .pendingDocuments(0)
                    .approvedDocuments(0)
                    .build();
        }

        List<String> businessIds = businesses.stream().map(Business::getId).toList();

        List<Package> packages = packageRepository.findByBusinessIdIn(businessIds);
        Set<String> packageIds = packages.stream().map(Package::getId).collect(Collectors.toSet());

        List<Booking> bookings = packageIds.isEmpty()
                ? Collections.emptyList()
                : bookingRepository.findByTripIdIn(packageIds.stream().toList());

        Set<String> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toSet());

        double revenue = paymentRepository.findAll().stream()
                .filter(p -> "SUCCESS".equals(p.getPaymentStatus()))
                .filter(p -> bookingIds.contains(p.getBookingId()))
                .mapToDouble(p -> p.getAmount() == null ? 0.0 : p.getAmount())
                .sum();

        long pendingPackages = packages.stream().filter(p -> "PENDING".equals(p.getApprovalStatus())).count();

        long pendingDocuments = businesses.stream()
                .mapToLong(b -> vendorDocumentRepository.countByBusinessIdAndStatus(b.getId(), com.example.manud_jaya.model.dto.VerificationStatus.PENDING))
                .sum();

        long approvedDocuments = businesses.stream()
                .mapToLong(b -> vendorDocumentRepository.countByBusinessIdAndStatus(b.getId(), com.example.manud_jaya.model.dto.VerificationStatus.APPROVED))
                .sum();

        return PartnerDashboardAnalyticsResponse.builder()
                .totalRevenue(revenue)
                .activeListings(packages.stream().filter(p -> "APPROVED".equals(p.getApprovalStatus())).count())
                .totalBookings(bookings.size())
                .pendingPackages(pendingPackages)
                .pendingDocuments(pendingDocuments)
                .approvedDocuments(approvedDocuments)
                .build();
    }

    public AdminDashboardAnalyticsResponse getAdminAnalytics() {
        long pendingPartner = userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", "PENDING").size();
        long pendingPackage = packageRepository.countByApprovalStatus("PENDING");
        long pendingDeletion = packageRepository.findByDeletionRequestStatus(com.example.manud_jaya.model.dto.DeletionRequestStatus.PENDING).size();
        long pendingDocument = vendorDocumentRepository.countByStatus(com.example.manud_jaya.model.dto.VerificationStatus.PENDING);
        long activePartners = userRepository.countByRoleAndStatus("VENDOR", "ACTIVE");
        long activePackages = packageRepository.countByApprovalStatus("APPROVED");

        double platformRevenue = paymentRepository.findByPaymentStatus("SUCCESS").stream()
                .mapToDouble(p -> p.getAmount() == null ? 0.0 : p.getAmount())
                .sum();

        return AdminDashboardAnalyticsResponse.builder()
                .pendingPartnerApplications(pendingPartner)
                .pendingPackageApprovals(pendingPackage)
                .pendingDeletionRequests(pendingDeletion)
                .pendingDocumentVerifications(pendingDocument)
                .activePartners(activePartners)
                .activePackages(activePackages)
                .platformRevenue(platformRevenue)
                .build();
    }

    public ImpactAnalyticsResponse getVendorImpactAnalytics(String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        List<Business> businesses = businessRepository.findByVendorId(vendor.getId());
        if (businesses.isEmpty()) {
            return ImpactAnalyticsResponse.builder()
                    .totalDestinations(0)
                    .totalReviews(0)
                    .averageRating(0)
                    .totalConfirmedBookings(0)
                    .build();
        }

        List<String> businessIds = businesses.stream().map(Business::getId).toList();
        List<Destination> destinations = businessIds.stream()
                .flatMap(id -> destinationRepository.findByBusinessId(id).stream())
                .toList();

        List<String> destinationIds = destinations.stream().map(Destination::getId).toList();
        List<Review> reviews = destinationIds.isEmpty()
                ? Collections.emptyList()
                : reviewRepository.findByDestinationIdIn(destinationIds);

        List<Package> packages = packageRepository.findByBusinessIdIn(businessIds);
        Set<String> packageIds = packages.stream().map(Package::getId).collect(Collectors.toSet());
        long confirmedBookings = packageIds.isEmpty() ? 0 : bookingRepository.findByTripIdIn(packageIds.stream().toList())
                .stream().filter(b -> "CONFIRMED".equals(b.getBookingStatus())).count();

        double averageRating = reviews.isEmpty() ? 0 : reviews.stream()
                .mapToDouble(r -> r.getRating() == null ? 0 : r.getRating())
                .average()
                .orElse(0);

        return ImpactAnalyticsResponse.builder()
                .totalDestinations(destinations.size())
                .totalReviews(reviews.size())
                .averageRating(averageRating)
                .totalConfirmedBookings(confirmedBookings)
                .build();
    }
}
