package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.Destination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends MongoRepository<Destination, String> {

    List<Destination> findByBusinessId(String businessId);

    List<Destination> findByApprovalStatus(String approvalStatus);

    List<Destination> findByBusinessIdAndApprovalStatus(String businessId, String approvalStatus);

    Optional<Destination> findByIdAndApprovalStatus(String id, String approvalStatus);
}
