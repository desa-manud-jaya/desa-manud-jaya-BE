package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.entity.VendorDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorDocumentRepository extends MongoRepository<VendorDocument, String> {

    List<VendorDocument> findByBusinessId(String businessId);

    List<VendorDocument> findByVendorIdAndBusinessId(String vendorId, String businessId);

    List<VendorDocument> findByStatus(VerificationStatus status);

    List<VendorDocument> findByStatus(VerificationStatus status, Pageable pageable);

    long countByBusinessIdAndStatus(String businessId, VerificationStatus status);

    long countByStatus(VerificationStatus status);

    List<VendorDocument> findByStatusAndDocumentType(VerificationStatus status, VendorDocumentType documentType, Pageable pageable);
}
