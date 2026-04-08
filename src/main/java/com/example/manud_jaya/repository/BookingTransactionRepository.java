package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.BookingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingTransactionRepository extends MongoRepository<BookingTransaction, String> {

    Page<BookingTransaction> findByUserId(String userId, Pageable pageable);

    Page<BookingTransaction> findByStatus(String status, Pageable pageable);

    Page<BookingTransaction> findByBusinessIdInAndStatus(List<String> businessIds, String status, Pageable pageable);

    List<BookingTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
