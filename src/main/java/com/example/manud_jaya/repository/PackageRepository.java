package com.example.manud_jaya.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.manud_jaya.model.entity.Package;

import java.util.List;
import java.util.Optional;

public interface PackageRepository extends MongoRepository<Package, String> {

    List<Package> findByBusinessId(String businessId);

    List<Package> findByApprovalStatus(String approvalStatus);

    List<Package> findByBusinessIdAndApprovalStatus(String businessId, String approvalStatus);

    Optional<Package> findByIdAndApprovalStatus(String id, String approvalStatus);
}
