package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorPackageService {

    private final PackageRepository repository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public Package createPackage(
            CreatePackageRequest request,
            String businessId,
            String username,
            String docUrl,
            String photoUrl
    ) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        Package pkg = Package.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .duration(request.getDuration())
                .availability(request.getAvailability())
                .itinerary(request.getItinerary())
                .included(request.getIncluded())
                .requirementDocumentUrl(docUrl)
                .photoUrl(photoUrl)
                .approvalStatus(ApprovalStatus.PENDING.toString())
                .vendorId(vendor.getId())
                .businessId(businessId)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(pkg);
    }

    public void approve(String id) {
        Package pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.APPROVED.toString());
        pkg.setRejectionReason(null);
        repository.save(pkg);
    }

    public void reject(String id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Rejection reason is required");
        }

        Package pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.REJECTED.toString());
        pkg.setRejectionReason(reason.trim());
        repository.save(pkg);
    }

    public List<Package> getByBusinessForVendor(String businessId, String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        return repository.findByBusinessId(businessId);
    }

    public List<Package> getPackageByStatus(String businessId, ApprovalStatus status) {
        return repository.findByBusinessIdAndApprovalStatus(businessId, status.toString());
    }

    public List<Package> getByBusiness(String businessId) {
        return repository.findByBusinessId(businessId);
    }

    public List<Package> getPending() {
        return repository.findByApprovalStatus(ApprovalStatus.PENDING.toString());
    }

    public List<Package> getApprovedByBusiness(String businessId) {
        return repository.findByBusinessIdAndApprovalStatus(businessId, ApprovalStatus.APPROVED.toString());
    }

    public Package getApprovedDetail(String packageId) {
        return repository.findByIdAndApprovalStatus(packageId, ApprovalStatus.APPROVED.toString())
                .orElseThrow(() -> new RuntimeException("Package not found"));
    }

    private void validateBusinessOwnership(User vendor, String businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
    }
}
