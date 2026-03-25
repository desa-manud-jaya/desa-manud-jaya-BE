package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment> findByPaymentStatus(String status, Pageable pageable);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByPaymentStatus(String status);
}
