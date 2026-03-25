package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId, Pageable pageable);

    Optional<Booking> findByTripId(String tripId);

    List<Booking> findByTripIdIn(List<String> tripIds);

    List<Booking> findByBookingStatus(String status, Pageable pageable);

    List<Booking> findByUserId(String userId);
}
