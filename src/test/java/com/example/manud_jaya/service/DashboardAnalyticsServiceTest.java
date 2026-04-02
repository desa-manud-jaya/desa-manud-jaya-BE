package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Destination;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.Payment;
import com.example.manud_jaya.model.entity.Review;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.DestinationRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.PaymentRepository;
import com.example.manud_jaya.repository.ReviewRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class DashboardAnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private VendorDocumentRepository vendorDocumentRepository;
    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private DashboardAnalyticsService dashboardAnalyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPartnerAnalyticsNoBusinessShouldReturnZeroes() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.of(User.builder().id("v1").build()));
        when(businessRepository.findByVendorId("v1")).thenReturn(List.of());

        var res = dashboardAnalyticsService.getPartnerAnalytics("vendor");
        assertEquals(0, res.getTotalBookings());
        assertEquals(0, res.getTotalRevenue());
    }

    @Test
    void getPartnerAnalyticsShouldCalculateValues() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.of(User.builder().id("v1").build()));
        when(businessRepository.findByVendorId("v1")).thenReturn(List.of(Business.builder().id("b1").build()));
        when(packageRepository.findByBusinessIdIn(List.of("b1"))).thenReturn(List.of(
                Package.builder().id("p1").approvalStatus("PENDING").build(),
                Package.builder().id("p2").approvalStatus("APPROVED").build()
        ));
        when(bookingRepository.findByTripIdIn(List.of("p1", "p2"))).thenReturn(List.of(Booking.builder().id("bk1").build()));
        when(paymentRepository.findAll()).thenReturn(List.of(
                Payment.builder().bookingId("bk1").paymentStatus("SUCCESS").amount(100.0).build(),
                Payment.builder().bookingId("bk1").paymentStatus("FAILED").amount(999.0).build()
        ));
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b1", VerificationStatus.PENDING)).thenReturn(2L);
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b1", VerificationStatus.APPROVED)).thenReturn(3L);

        var res = dashboardAnalyticsService.getPartnerAnalytics("vendor");
        assertEquals(100.0, res.getTotalRevenue());
        assertEquals(1, res.getTotalBookings());
        assertEquals(1, res.getPendingPackages());
        assertEquals(2, res.getPendingDocuments());
        assertEquals(3, res.getApprovedDocuments());
        assertEquals(1, res.getActiveListings());
    }

    @Test
    void getPartnerAnalyticsVendorNotFoundShouldThrow() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> dashboardAnalyticsService.getPartnerAnalytics("vendor"));
    }

    @Test
    void getAdminAnalyticsShouldCalculateValues() {
        when(userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", "PENDING")).thenReturn(List.of(User.builder().id("v1").build()));
        when(packageRepository.countByApprovalStatus("PENDING")).thenReturn(2L);
        when(packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING)).thenReturn(List.of(Package.builder().id("p1").build()));
        when(vendorDocumentRepository.countByStatus(VerificationStatus.PENDING)).thenReturn(4L);
        when(userRepository.countByRoleAndStatus("VENDOR", "ACTIVE")).thenReturn(5L);
        when(packageRepository.countByApprovalStatus("APPROVED")).thenReturn(6L);
        when(paymentRepository.findByPaymentStatus("SUCCESS")).thenReturn(List.of(Payment.builder().amount(100.0).build(), Payment.builder().amount(null).build()));

        var res = dashboardAnalyticsService.getAdminAnalytics();
        assertEquals(1, res.getPendingPartnerApplications());
        assertEquals(100.0, res.getPlatformRevenue());
        assertEquals(6, res.getActivePackages());
    }

    @Test
    void getVendorImpactAnalyticsNoBusinessShouldReturnZeroes() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.of(User.builder().id("v1").build()));
        when(businessRepository.findByVendorId("v1")).thenReturn(List.of());

        var res = dashboardAnalyticsService.getVendorImpactAnalytics("vendor");
        assertEquals(0, res.getTotalDestinations());
        assertEquals(0, res.getAverageRating());
    }

    @Test
    void getVendorImpactAnalyticsShouldCalculate() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.of(User.builder().id("v1").build()));
        when(businessRepository.findByVendorId("v1")).thenReturn(List.of(Business.builder().id("b1").build()));
        when(destinationRepository.findByBusinessId("b1")).thenReturn(List.of(Destination.builder().id("d1").build()));
        when(reviewRepository.findByDestinationIdIn(List.of("d1"))).thenReturn(List.of(
                Review.builder().rating(4).build(),
                Review.builder().rating(2).build()
        ));
        when(packageRepository.findByBusinessIdIn(List.of("b1"))).thenReturn(List.of(Package.builder().id("p1").build()));
        when(bookingRepository.findByTripIdIn(List.of("p1"))).thenReturn(List.of(
                Booking.builder().bookingStatus("CONFIRMED").build(),
                Booking.builder().bookingStatus("PENDING").build()
        ));

        var res = dashboardAnalyticsService.getVendorImpactAnalytics("vendor");
        assertEquals(1, res.getTotalDestinations());
        assertEquals(2, res.getTotalReviews());
        assertEquals(3.0, res.getAverageRating());
        assertEquals(1, res.getTotalConfirmedBookings());
    }

    @Test
    void getVendorImpactAnalyticsVendorNotFoundShouldThrow() {
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> dashboardAnalyticsService.getVendorImpactAnalytics("vendor"));
    }
}
