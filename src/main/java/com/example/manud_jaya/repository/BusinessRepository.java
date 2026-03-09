package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends MongoRepository<Business, String> {

    List<Business> findByVendorId(String vendorId);

    Page<Business> findByApprovalStatus(String approvalStatus, Pageable pageable);

    Optional<Business> findByIdAndApprovalStatus(String id, String approvalStatus);

}
