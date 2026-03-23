package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.manud_jaya.model.entity.Package;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorPackageService {

    private final PackageRepository repository;

    public Package createPackage(CreatePackageRequest request,
                                       String vendorId,
                                       String businessId,
                                       String docUrl,
                                       String photoUrl) {

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
                .vendorId(vendorId)
                .businessId(businessId)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(pkg);
    }

    public void approve(String id) {
        Package pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.APPROVED.toString());
        repository.save(pkg);
    }

    public void reject(String id, String reason) {
        Package pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.REJECTED.toString());
        pkg.setRejectionReason(reason);
        repository.save(pkg);
    }

    public List<Package> getByBusiness(String businessId) {
        return repository.findByBusinessId(businessId);
    }

    public List<Package> getPackageByStatus(String businessId, ApprovalStatus status) {
        return repository.findByBusinessIdAndApprovalStatus(businessId, status.toString());
    }

    public List<Package> getPending() {
        return repository.findByApprovalStatus(ApprovalStatus.PENDING.toString());
    }
}
