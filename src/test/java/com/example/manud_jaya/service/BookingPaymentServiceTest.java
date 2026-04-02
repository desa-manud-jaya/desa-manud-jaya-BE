package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.TransactionException;
import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Payment;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingPaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingPaymentService bookingPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBookingWithPaymentShouldSaveBoth() throws Exception {
        Booking booking = Booking.builder().userId("u1").build();
        Payment payment = Payment.builder().amount(100.0).build();
        when(bookingRepository.save(booking)).thenReturn(Booking.builder().id("b1").userId("u1").build());

        Booking result = bookingPaymentService.createBookingWithPayment(booking, payment);

        assertEquals("b1", result.getId());
        assertEquals("b1", payment.getBookingId());
        verify(paymentRepository).save(payment);
    }

    @Test
    void createBookingWithPaymentFailureShouldThrowTransactionException() {
        Booking booking = Booking.builder().userId("u1").build();
        Payment payment = Payment.builder().build();
        when(bookingRepository.save(booking)).thenThrow(new RuntimeException("db"));

        assertThrows(TransactionException.class, () -> bookingPaymentService.createBookingWithPayment(booking, payment));
    }

    @Test
    void completeBookingPaymentSuccessShouldConfirmWhenSuccessStatus() throws Exception {
        Booking booking = Booking.builder().id("b1").bookingStatus("PENDING").build();
        Payment payment = Payment.builder().bookingId("b1").paymentStatus("PENDING").build();
        when(bookingRepository.findById("b1")).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId("b1")).thenReturn(Optional.of(payment));

        bookingPaymentService.completeBookingPayment("b1", "tx1", "SUCCESS");

        assertEquals("CONFIRMED", booking.getBookingStatus());
        assertEquals("tx1", payment.getTransactionId());
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
    }

    @Test
    void completeBookingPaymentNonSuccessShouldNotConfirm() throws Exception {
        Booking booking = Booking.builder().id("b1").bookingStatus("PENDING").build();
        Payment payment = Payment.builder().bookingId("b1").paymentStatus("PENDING").build();
        when(bookingRepository.findById("b1")).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId("b1")).thenReturn(Optional.of(payment));

        bookingPaymentService.completeBookingPayment("b1", "tx1", "FAILED");

        assertEquals("PENDING", booking.getBookingStatus());
    }

    @Test
    void completeBookingPaymentBookingNotFoundShouldThrow() {
        when(bookingRepository.findById("b1")).thenReturn(Optional.empty());

        assertThrows(TransactionException.class,
                () -> bookingPaymentService.completeBookingPayment("b1", "tx", "SUCCESS"));
        verify(paymentRepository, never()).findByBookingId(any());
    }

    @Test
    void completeBookingPaymentPaymentNotFoundShouldThrow() {
        when(bookingRepository.findById("b1")).thenReturn(Optional.of(Booking.builder().id("b1").build()));
        when(paymentRepository.findByBookingId("b1")).thenReturn(Optional.empty());

        assertThrows(TransactionException.class,
                () -> bookingPaymentService.completeBookingPayment("b1", "tx", "SUCCESS"));
    }
}
