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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @InjectMocks
    private BookingTransactionService bookingTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(bookingTransactionRepository.existsByGuideIdAndTripDateAndStatusIn(anyString(), any(), anyList()))
                .thenReturn(false);
    }

    @Test
    void createBookingShouldSetWaitingForPaymentStatus() {
        User user = User.builder().id("user-1").username("user1").build();
        Business business = Business.builder().id("biz-1").build();
        Package pkg = Package.builder().id("pkg-1").businessId("biz-1").guideId("guide-1").price(BigDecimal.valueOf(100000)).build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .businessId("biz-1")
                .packageId("pkg-1")
                .tripDate("2026-05-01")
                .quantity(2)
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(businessRepository.findById("biz-1")).thenReturn(Optional.of(business));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(bookingTransactionRepository.save(any(BookingTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.createBooking("user1", request);

        assertEquals("waiting_for_payment", result.getStatus());
        assertEquals(200000.0, result.getAmount());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void createBookingMissingRequestShouldThrowValidation() {
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", null));
    }

    @Test
    void createBookingMissingBusinessShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().packageId("pkg").quantity(1).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingMissingPackageShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").quantity(1).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingInvalidQuantityShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").quantity(0).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingBlankBusinessShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId(" ").packageId("pkg").quantity(1).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingBlankPackageShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId(" ").quantity(1).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingNullQuantityShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").quantity(null).build();
        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingUserNotFoundShouldThrowNotFound() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").tripDate("2026-05-01").quantity(1).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingBusinessNotFoundShouldThrowNotFound() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").tripDate("2026-05-01").quantity(1).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("u").build()));
        when(businessRepository.findById("biz")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingPackageNotFoundShouldThrowNotFound() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").tripDate("2026-05-01").quantity(1).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("u").build()));
        when(businessRepository.findById("biz")).thenReturn(Optional.of(Business.builder().id("biz").build()));
        when(packageRepository.findById("pkg")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingPackageBusinessMismatchShouldThrowValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").tripDate("2026-05-01").quantity(1).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("u").build()));
        when(businessRepository.findById("biz")).thenReturn(Optional.of(Business.builder().id("biz").build()));
        when(packageRepository.findById("pkg")).thenReturn(Optional.of(Package.builder().id("pkg").businessId("other").price(BigDecimal.ONE).build()));

        assertThrows(ValidationException.class, () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void createBookingNullPriceShouldUseZeroAmount() {
        CreateBookingRequest request = CreateBookingRequest.builder().businessId("biz").packageId("pkg").tripDate("2026-05-01").quantity(3).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("u").build()));
        when(businessRepository.findById("biz")).thenReturn(Optional.of(Business.builder().id("biz").build()));
        when(packageRepository.findById("pkg")).thenReturn(Optional.of(Package.builder().id("pkg").businessId("biz").guideId("guide-1").price(null).build()));
        when(bookingTransactionRepository.save(any(BookingTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.createBooking("user1", request);
        assertEquals(0.0, result.getAmount());
    }

    @Test
    void createBookingGuideOccupiedShouldThrowConflict() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .businessId("biz")
                .packageId("pkg")
                .tripDate("2026-05-01")
                .quantity(1)
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("u1").build()));
        when(businessRepository.findById("biz")).thenReturn(Optional.of(Business.builder().id("biz").build()));
        when(packageRepository.findById("pkg")).thenReturn(Optional.of(Package.builder().id("pkg").businessId("biz").guideId("guide-1").price(BigDecimal.ONE).build()));
        when(bookingTransactionRepository.existsByGuideIdAndTripDateAndStatusIn(anyString(), any(), anyList()))
                .thenReturn(true);

        assertThrows(com.example.manud_jaya.exception.ConflictException.class,
                () -> bookingTransactionService.createBooking("user1", request));
    }

    @Test
    void getGuideBookingsShouldReturnData() {
        User guide = User.builder().id("g1").username("guide1").role("GUIDE").build();
        when(userRepository.findByUsername("guide1")).thenReturn(Optional.of(guide));
        when(bookingTransactionRepository.findByGuideId("g1", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(BookingTransaction.builder().id("trx-1").guideId("g1").build()), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getGuideBookings("guide1", 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void uploadPaymentProofShouldMoveToPending() throws Exception {
        User user = User.builder().id("user-1").username("user1").build();
        BookingTransaction trx = BookingTransaction.builder().id("trx-1").userId("user-1").status("waiting_for_payment").build();
        MultipartFile file = mock(MultipartFile.class);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(bookingTransactionRepository.findById("trx-1")).thenReturn(Optional.of(trx));
        when(supabaseStorageService.uploadDocument(file)).thenReturn("https://example/proof.pdf");
        when(bookingTransactionRepository.save(any(BookingTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.uploadPaymentProof("user1", "trx-1", file);

        assertEquals("pending", result.getStatus());
        assertEquals("https://example/proof.pdf", result.getPaymentProofUrl());
        assertNotNull(result.getPaymentUploadedAt());
    }

    @Test
    void uploadPaymentProofUserNotFoundShouldThrow() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.uploadPaymentProof("user1", "trx-1", mock(MultipartFile.class)));
    }

    @Test
    void uploadPaymentProofBookingNotFoundShouldThrow() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("user-1").build()));
        when(bookingTransactionRepository.findById("trx-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.uploadPaymentProof("user1", "trx-1", mock(MultipartFile.class)));
    }

    @Test
    void uploadPaymentProofWrongOwnerShouldThrowUnauthorized() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("user-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").userId("user-2").status("waiting_for_payment").build()));

        assertThrows(UnauthorizedException.class,
                () -> bookingTransactionService.uploadPaymentProof("user1", "trx-1", mock(MultipartFile.class)));
    }

    @Test
    void uploadPaymentProofWrongStatusShouldThrowValidation() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("user-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").userId("user-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.uploadPaymentProof("user1", "trx-1", mock(MultipartFile.class)));
    }

    @Test
    void uploadPaymentProofUploadFailShouldThrowValidation() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("user-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").userId("user-1").status("waiting_for_payment").build()));
        doThrow(new IOException("io")).when(supabaseStorageService).uploadDocument(file);

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.uploadPaymentProof("user1", "trx-1", file));
        verify(bookingTransactionRepository, never()).save(any());
    }

    @Test
    void getUserBookingHistoryShouldRejectDifferentUserId() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(User.builder().id("user-1").build()));

        assertThrows(UnauthorizedException.class,
                () -> bookingTransactionService.getUserBookingHistory("user1", "user-2", 0, 10));
    }

    @Test
    void getUserBookingHistoryUserNotFoundShouldThrow() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.getUserBookingHistory("user1", "user-1", 0, 10));
    }

    @Test
    void getUserBookingHistoryShouldValidatePagination() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getUserBookingHistory("user1", "user-1", -1, 10));
    }

    @Test
    void getUserBookingHistoryShouldReturnData() {
        User currentUser = User.builder().id("user-1").username("user1").build();
        BookingTransaction transaction = BookingTransaction.builder().id("trx-1").userId("user-1").amount(10.0).build();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(currentUser));
        when(bookingTransactionRepository.findByUserId("user-1", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getUserBookingHistory("user1", "user-1", 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getAdminBookingPaymentsShouldReturnAllWhenStatusBlank() {
        when(bookingTransactionRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(BookingTransaction.builder().id("1").build()), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getAdminBookingPayments("", 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getAdminBookingPaymentsShouldReturnAllWhenStatusNull() {
        when(bookingTransactionRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(BookingTransaction.builder().id("1").build()), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getAdminBookingPayments(null, 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getAdminBookingPaymentsShouldFilterByStatus() {
        when(bookingTransactionRepository.findByStatus("pending", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(BookingTransaction.builder().id("1").status("pending").build()), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getAdminBookingPayments("PENDING", 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getAdminBookingPaymentsShouldValidatePagination() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getAdminBookingPayments(null, 0, 0));
    }

    @Test
    void getAdminBookingPaymentDetailShouldReturnData() {
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").build()));

        BookingTransaction result = bookingTransactionService.getAdminBookingPaymentDetail("trx-1");
        assertEquals("trx-1", result.getId());
    }

    @Test
    void getAdminBookingPaymentDetailNotFoundShouldThrow() {
        when(bookingTransactionRepository.findById("trx-1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.getAdminBookingPaymentDetail("trx-1"));
    }

    @Test
    void reviewBookingPaymentShouldApprove() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));
        when(bookingTransactionRepository.save(any(BookingTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.reviewBookingPayment("admin", "trx-1", "APPROVE", "ok");
        assertEquals("approved", result.getStatus());
    }

    @Test
    void reviewBookingPaymentShouldRejectWithNote() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));
        when(bookingTransactionRepository.save(any(BookingTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingTransaction result = bookingTransactionService.reviewBookingPayment("admin", "trx-1", "REJECT", "invalid proof");
        assertEquals("rejected", result.getStatus());
        assertEquals("invalid proof", result.getReviewNote());
    }

    @Test
    void reviewBookingPaymentAdminNotFoundShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "APPROVE", "ok"));
    }

    @Test
    void reviewBookingPaymentBookingNotFoundShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "APPROVE", "ok"));
    }

    @Test
    void reviewBookingPaymentStatusNotPendingShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("approved").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "APPROVE", "ok"));
    }

    @Test
    void reviewBookingPaymentMissingDecisionShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "", "ok"));
    }

    @Test
    void reviewBookingPaymentNullDecisionShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", null, "ok"));
    }

    @Test
    void reviewBookingPaymentRejectWithoutNoteShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "REJECT", null));
    }

    @Test
    void reviewBookingPaymentRejectWithBlankNoteShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "REJECT", "   "));
    }

    @Test
    void reviewBookingPaymentInvalidDecisionShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("admin-1").build()));
        when(bookingTransactionRepository.findById("trx-1"))
                .thenReturn(Optional.of(BookingTransaction.builder().id("trx-1").status("pending").build()));

        assertThrows(ValidationException.class,
                () -> bookingTransactionService.reviewBookingPayment("admin", "trx-1", "HOLD", "ok"));
    }

    @Test
    void getVendorApprovedBookingsShouldReturnEmptyWhenNoBusiness() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(User.builder().id("vendor-1").build()));
        when(businessRepository.findByVendorId("vendor-1")).thenReturn(List.of());

        PagedResponse<BookingTransaction> response = bookingTransactionService.getVendorApprovedBookings("vendor1", 0, 10);
        assertEquals(0, response.getTotal());
    }

    @Test
    void getVendorApprovedBookingsShouldReturnApprovedOnly() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(User.builder().id("vendor-1").build()));
        when(businessRepository.findByVendorId("vendor-1"))
                .thenReturn(List.of(Business.builder().id("biz-1").vendorId("vendor-1").build()));
        when(bookingTransactionRepository.findByBusinessIdInAndStatus(List.of("biz-1"), "approved", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(BookingTransaction.builder().id("trx-1").status("approved").build()), PageRequest.of(0, 10), 1));

        PagedResponse<BookingTransaction> response = bookingTransactionService.getVendorApprovedBookings("vendor1", 0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getVendorApprovedBookingsVendorNotFoundShouldThrow() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingTransactionService.getVendorApprovedBookings("vendor1", 0, 10));
    }

    @Test
    void getVendorApprovedBookingsInvalidPagingShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getVendorApprovedBookings("vendor1", 0, 0));
    }

    @Test
    void getRevenueSummaryShouldReturnAllWhenDatesEmpty() {
        when(bookingTransactionRepository.findAll())
                .thenReturn(List.of(
                        BookingTransaction.builder().amount(10.0).build(),
                        BookingTransaction.builder().amount(null).build()
                ));

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary(null, null);
        assertEquals(10.0, response.getTotalRevenue());
        assertEquals(2, response.getTransactionCount());
    }

    @Test
    void getRevenueSummaryShouldReturnAllWhenDatesBlank() {
        when(bookingTransactionRepository.findAll())
                .thenReturn(List.of(BookingTransaction.builder().amount(5.0).build()));

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary("   ", " ");
        assertEquals(5.0, response.getTotalRevenue());
        assertEquals(1, response.getTransactionCount());
    }

    @Test
    void getRevenueSummaryShouldReturnAllWhenStartNullEndBlank() {
        when(bookingTransactionRepository.findAll())
                .thenReturn(List.of(BookingTransaction.builder().amount(1.0).build()));

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary(null, " ");
        assertEquals(1.0, response.getTotalRevenue());
    }

    @Test
    void getRevenueSummaryShouldReturnAllWhenStartBlankEndNull() {
        when(bookingTransactionRepository.findAll())
                .thenReturn(List.of(BookingTransaction.builder().amount(2.0).build()));

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary(" ", null);
        assertEquals(2.0, response.getTotalRevenue());
    }

    @Test
    void getRevenueSummaryShouldApplyDateRangeFilter() {
        when(bookingTransactionRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        BookingTransaction.builder().amount(100000.0).build(),
                        BookingTransaction.builder().amount(250000.0).build()
                ));

        RevenueSummaryResponse response = bookingTransactionService.getRevenueSummary("2026-03-01", "2026-03-31");

        assertEquals(350000.0, response.getTotalRevenue());
        assertEquals(2, response.getTransactionCount());
    }

    @Test
    void getRevenueSummaryMissingOneDateShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("2026-03-01", null));
    }

    @Test
    void getRevenueSummaryMissingOneDateBlankVariantShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("   ", "2026-03-31"));
    }

    @Test
    void getRevenueSummaryMissingOneDateStartNullShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary(null, "2026-03-31"));
    }

    @Test
    void getRevenueSummaryMissingOneDateEndBlankShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("2026-03-31", "   "));
    }

    @Test
    void getRevenueSummaryInvalidDateFormatShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("03-01-2026", "2026-03-31"));
    }

    @Test
    void getRevenueSummaryEndBeforeStartShouldThrow() {
        assertThrows(ValidationException.class,
                () -> bookingTransactionService.getRevenueSummary("2026-04-01", "2026-03-31"));
    }
}
