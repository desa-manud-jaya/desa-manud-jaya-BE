package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.TripPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripPlanRepository extends MongoRepository<TripPlan, String> {
    List<TripPlan> findByUserId(String userId, Pageable pageable);
    List<TripPlan> findByUserId(String userId);
}
