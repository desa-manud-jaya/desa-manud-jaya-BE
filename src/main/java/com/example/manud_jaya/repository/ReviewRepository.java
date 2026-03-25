package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByUserId(String userId, Pageable pageable);

    List<Review> findByDestinationId(String destinationId, Pageable pageable);

    Optional<Review> findByUserIdAndDestinationId(String userId, String destinationId);

    List<Review> findByDestinationId(String destinationId);

    List<Review> findByDestinationIdIn(List<String> destinationIds);

    List<Review> findByUserId(String userId);
}
