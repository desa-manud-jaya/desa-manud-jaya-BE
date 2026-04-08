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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class VendorOperationsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BookingTransactionRepository bookingTransactionRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private VendorOperationsService vendorOperationsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getVendorBookingsShouldReturnApprovedOnlyFromTransactions() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        Business business = Business.builder().id("biz-1").vendorId("vendor-1").build();
        BookingTransaction booking = BookingTransaction.builder()
                .id("trx-1")
                .businessId("biz-1")
                .status("approved")
                .build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(List.of(business));
        when(bookingTransactionRepository.findByBusinessIdInAndStatus(List.of("biz-1"), "approved", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(booking), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = vendorOperationsService.getVendorBookings("vendor1", 0, 10);

        assertEquals(1, response.getTotal());
        assertEquals("approved", response.getItems().get(0).getStatus());
    }

    @Test
    void getVendorBookingsNoBusinessShouldReturnEmpty() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(List.of());

        PagedResponse<BookingTransaction> response = vendorOperationsService.getVendorBookings("vendor1", 0, 10);
        assertEquals(0, response.getTotal());
    }

    @Test
    void getVendorBookingsInvalidPageShouldThrow() {
        assertThrows(ValidationException.class, () -> vendorOperationsService.getVendorBookings("vendor1", -1, 10));
        assertThrows(ValidationException.class, () -> vendorOperationsService.getVendorBookings("vendor1", 0, 0));
    }

    @Test
    void getVendorReviewsNoBusinessShouldReturnEmpty() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(List.of());

        PagedResponse<Review> response = vendorOperationsService.getVendorReviews("vendor1", 0, 10);
        assertEquals(0, response.getTotal());
    }

    @Test
    void getVendorReviewsShouldReturnPaginatedSubset() {
        User vendor = User.builder().id("vendor-1").username("vendor1").build();
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(List.of(Business.builder().id("biz-1").build()));
        when(destinationRepository.findByBusinessId("biz-1")).thenReturn(List.of(Destination.builder().id("d1").build()));
        when(reviewRepository.findByDestinationIdIn(List.of("d1"))).thenReturn(List.of(
                Review.builder().id("r1").build(),
                Review.builder().id("r2").build()
        ));

        PagedResponse<Review> response = vendorOperationsService.getVendorReviews("vendor1", 0, 1);
        assertEquals(2, response.getTotal());
        assertEquals(1, response.getItems().size());
    }
}
