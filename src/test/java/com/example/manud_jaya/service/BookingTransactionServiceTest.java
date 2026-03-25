package com.example.manud_jaya.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BookingTransactionServiceTest {

    @Mock
    private BookingTransactionRepository bookingTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private BookingTransactionService bookingTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBookingShouldSetPendingStatusAndSaveData() {
        User user = User.builder().id("user-1").username("user1").build();
        Business business = Business.builder().id("biz-1").build();
        Package pkg = Package.builder().id("pkg-1").businessId("biz-1").price(BigDecimal.valueOf(100000)).build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .businessId("biz-1")
                .packageId("pkg-1")
                .quantity(2)
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(businessRepository.findById("biz-1")).thenReturn(Optional.of(business));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(bookingTransactionRepository.save(any(BookingTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.createBooking("user1", request);

        assertEquals("pending", result.getStatus());
        assertEquals("user-1", result.getUserId());
        assertEquals("biz-1", result.getBusinessId());
        assertEquals(200000.0, result.getAmount());
    }

    @Test
    void getUserBookingHistoryShouldRejectDifferentUserId() {
        User currentUser = User.builder().id("user-1").username("user1").build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(currentUser));

        assertThrows(UnauthorizedException.class,
                () -> bookingTransactionService.getUserBookingHistory("user1", "user-2", 0, 10));
    }

    @Test
    void getUserBookingHistoryShouldReturnOnlyRequestedUserData() {
        User currentUser = User.builder().id("user-1").username("user1").build();
        BookingTransaction transaction = BookingTransaction.builder()
                .id("trx-1")
                .userId("user-1")
                .amount(150000.0)
                .status("pending")
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(currentUser));
        when(bookingTransactionRepository.findByUserId("user-1", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getUserBookingHistory("user1", "user-1", 0, 10);

        assertEquals(1, response.getTotal());
        assertEquals("user-1", response.getItems().get(0).getUserId());
    }

    @Test
    void getRevenueSummaryShouldApplyDateRangeFilter() {
        List<BookingTransaction> transactions = List.of(
                BookingTransaction.builder().id("trx-1").amount(100000.0).createdAt(LocalDateTime.now()).build(),
                BookingTransaction.builder().id("trx-2").amount(250000.0).createdAt(LocalDateTime.now()).build()
        );

        when(bookingTransactionRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary("2026-03-01", "2026-03-31");

        assertEquals(350000.0, response.getTotalRevenue());
        assertEquals(2, response.getTransactionCount());
        assertEquals("2026-03-01", response.getStartDate());
        assertEquals("2026-03-31", response.getEndDate());
    }

    @Test
    void getRevenueSummaryShouldRejectInvalidDateFormat() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("03-01-2026", "2026-03-31"));
    }
}
