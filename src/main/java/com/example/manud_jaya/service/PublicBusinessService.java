package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicBusinessService {

    private final BusinessRepository businessRepository;

    public Page<Business> getApprovedBusinesses(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return businessRepository.findByApprovalStatus("APPROVED", pageable);
    }

    public Business getBusinessDetail(String businessId) {

        return businessRepository
                .findByIdAndApprovalStatus(businessId, "APPROVED")
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    public Business getBusinessByVendor(String vendorId) {

        return businessRepository
                .findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("don't find business"));
    }
}
