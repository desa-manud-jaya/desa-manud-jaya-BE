package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.manud_jaya.model.entity.Package;
import java.util.List;

public interface PackageRepository extends MongoRepository<Package, String> {

    List<Package> findByBusinessId(String businessId);

    List<Package> findByApprovalStatus(String approvalStatus);

    List<Package> findByBusinessIdAndApprovalStatus(String businessId, String approvalStatus);



}
