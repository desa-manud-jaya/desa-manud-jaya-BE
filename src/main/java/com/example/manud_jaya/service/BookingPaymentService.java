package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.TransactionException;
import com.example.manud_jaya.model.entity.Booking;
import com.example.manud_jaya.model.entity.Payment;
import com.example.manud_jaya.repository.BookingRepository;
import com.example.manud_jaya.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Booking createBookingWithPayment(Booking booking, Payment payment) throws TransactionException {
        try {
            log.info("Creating booking with payment for user: {}", booking.getUserId());
            
            Booking savedBooking = bookingRepository.save(booking);
            log.info("Booking created successfully with id: {}", savedBooking.getId());
            
            payment.setBookingId(savedBooking.getId());
            paymentRepository.save(payment);
            log.info("Payment record created for booking: {}", savedBooking.getId());
            
            return savedBooking;
        } catch (Exception e) {
            log.error("Transaction failed for booking", e);
            throw new TransactionException("Failed to create booking with payment", e);
        }
    }

    @Transactional
    public void completeBookingPayment(String bookingId, String transactionId, String paymentStatus) throws TransactionException {
        try {
            log.info("Completing payment for booking: {}", bookingId);
            
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new TransactionException("Booking not found with id: " + bookingId));
            
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> new TransactionException("Payment not found for booking: " + bookingId));
            
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(paymentStatus);
            paymentRepository.save(payment);
            
            if ("SUCCESS".equals(paymentStatus)) {
                booking.setBookingStatus("CONFIRMED");
                log.info("Booking confirmed for id: {}", bookingId);
            }
            
            bookingRepository.save(booking);
            log.info("Booking payment completed successfully");
        } catch (Exception e) {
            log.error("Error completing booking payment", e);
            throw new TransactionException("Failed to complete booking payment", e);
        }
    }
}
