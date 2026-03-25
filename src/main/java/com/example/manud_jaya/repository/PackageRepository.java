package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.entity.Package;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PackageRepository extends MongoRepository<Package, String> {

    List<Package> findByBusinessId(String businessId);

    List<Package> findByBusinessId(String businessId, Pageable pageable);

    List<Package> findByApprovalStatus(String approvalStatus);

    List<Package> findByApprovalStatus(String approvalStatus, Pageable pageable);

    List<Package> findByBusinessIdAndApprovalStatus(String businessId, String approvalStatus);

    List<Package> findByBusinessIdAndApprovalStatus(String businessId, String approvalStatus, Pageable pageable);

    List<Package> findByBusinessIdIn(List<String> businessIds);

    List<Package> findByBusinessIdInAndApprovalStatus(List<String> businessIds, String approvalStatus);

    List<Package> findByBusinessIdInAndDeletionRequestStatus(List<String> businessIds, DeletionRequestStatus status);

    List<Package> findByDeletionRequestStatus(DeletionRequestStatus status);

    List<Package> findByDeletionRequestStatus(DeletionRequestStatus status, Pageable pageable);

    Optional<Package> findByIdAndApprovalStatus(String id, String approvalStatus);

    long countByApprovalStatus(String approvalStatus);

    long countByBusinessIdInAndApprovalStatus(List<String> businessIds, String approvalStatus);
}
