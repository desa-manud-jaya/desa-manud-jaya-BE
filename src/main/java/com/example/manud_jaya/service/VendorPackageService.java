package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ConflictException;
import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.ValidationException;
import org.springframework.security.access.AccessDeniedException;
import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.GuideProfile;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.model.inbound.request.UpdatePackageRequest;
import com.example.manud_jaya.model.inbound.response.GuideSummaryResponse;
import com.example.manud_jaya.model.inbound.response.PackageDetailResponse;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VendorPackageService {

    private final PackageRepository repository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ModerationAuditService moderationAuditService;

    public Package createPackage(
            CreatePackageRequest request,
            String businessId,
            String username,
            String docUrl,
            String photoUrl
    ) {

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        Package pkg = Package.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .duration(request.getDuration())
                .availability(request.getAvailability())
                .itinerary(request.getItinerary())
                .included(request.getIncluded())
                .termsAndConditions(request.getTermsAndConditions())
                .pricingPolicy(request.getPricingPolicy())
                .cancellationPolicy(request.getCancellationPolicy())
                .requirementDocumentUrl(docUrl)
                .photoUrl(photoUrl)
                .approvalStatus(ApprovalStatus.PENDING.toString())
                .vendorId(vendor.getId())
                .businessId(businessId)
                .deletionRequestStatus(DeletionRequestStatus.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return repository.save(pkg);
    }

    public Package updatePackage(String businessId,
                                 String packageId,
                                 String username,
                                 UpdatePackageRequest request) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        Package pkg = repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        if (!pkg.getBusinessId().equals(businessId)) {
            throw new AccessDeniedException("Package does not belong to business");
        }

        if (request.getName() != null) pkg.setName(request.getName());
        if (request.getCategory() != null) pkg.setCategory(request.getCategory());
        if (request.getPrice() != null) pkg.setPrice(request.getPrice());
        if (request.getDuration() != null) pkg.setDuration(request.getDuration());
        if (request.getAvailability() != null) pkg.setAvailability(request.getAvailability());
        if (request.getItinerary() != null) pkg.setItinerary(request.getItinerary());
        if (request.getIncluded() != null) pkg.setIncluded(request.getIncluded());
        if (request.getTermsAndConditions() != null) pkg.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getPricingPolicy() != null) pkg.setPricingPolicy(request.getPricingPolicy());
        if (request.getCancellationPolicy() != null) pkg.setCancellationPolicy(request.getCancellationPolicy());

        pkg.setModerationNote(request.getModerationNote());
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        moderationAuditService.log("PACKAGE", "UPDATE", vendor.getId(), saved.getId(), "Vendor updated package");
        return saved;
    }

    public Package requestDeletion(String businessId,
                                   String packageId,
                                   String username,
                                   String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Deletion request reason is required");
        }

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        Package pkg = repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        if (!pkg.getBusinessId().equals(businessId)) {
            throw new AccessDeniedException("Package does not belong to business");
        }

        pkg.setDeletionRequestStatus(DeletionRequestStatus.PENDING);
        pkg.setDeletionRequestReason(reason.trim());
        pkg.setDeletionRequestedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        moderationAuditService.log("PACKAGE", "REQUEST_DELETION", vendor.getId(), saved.getId(), reason);
        return saved;
    }

    public void approve(String id) {
        approve(id, null, null);
    }

    public void approve(String id, String adminId, String note) {
        Package pkg = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.APPROVED.toString());
        pkg.setRejectionReason(null);
        pkg.setModerationNote(note);
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        if (adminId != null) {
            moderationAuditService.log("PACKAGE", "APPROVE", adminId, saved.getId(), note);
        }
    }

    public void reject(String id, String reason) {
        reject(id, reason, null);
    }

    public void reject(String id, String reason, String adminId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Rejection reason is required");
        }

        Package pkg = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        pkg.setApprovalStatus(ApprovalStatus.REJECTED.toString());
        pkg.setRejectionReason(reason.trim());
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        if (adminId != null) {
            moderationAuditService.log("PACKAGE", "REJECT", adminId, saved.getId(), reason);
        }
    }

    public Package approveDeletionRequest(String packageId, String adminId, String note) {
        Package pkg = repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        if (pkg.getDeletionRequestStatus() != DeletionRequestStatus.PENDING) {
            throw new ConflictException("No pending deletion request");
        }

        pkg.setDeletionRequestStatus(DeletionRequestStatus.APPROVED);
        pkg.setDeletionReviewNote(note);
        pkg.setDeletionReviewerId(adminId);
        pkg.setDeletionReviewedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        moderationAuditService.log("PACKAGE", "APPROVE_DELETION", adminId, saved.getId(), note);
        return saved;
    }

    public Package rejectDeletionRequest(String packageId, String adminId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Reason is required");
        }

        Package pkg = repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        if (pkg.getDeletionRequestStatus() != DeletionRequestStatus.PENDING) {
            throw new ConflictException("No pending deletion request");
        }

        pkg.setDeletionRequestStatus(DeletionRequestStatus.REJECTED);
        pkg.setDeletionReviewNote(reason.trim());
        pkg.setDeletionReviewerId(adminId);
        pkg.setDeletionReviewedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());

        Package saved = repository.save(pkg);
        moderationAuditService.log("PACKAGE", "REJECT_DELETION", adminId, saved.getId(), reason);
        return saved;
    }

    public List<Package> getByBusinessForVendor(String businessId, String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        return repository.findByBusinessId(businessId);
    }

    public PagedResponse<Package> getByBusinessForVendor(String businessId,
                                                         String username,
                                                         String approvalStatus,
                                                         DeletionRequestStatus deletionRequestStatus,
                                                         String q,
                                                         int page,
                                                         int size) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        validateBusinessOwnership(vendor, businessId);

        Pageable pageable = PageRequest.of(page, size);

        List<Package> source;

        if (approvalStatus != null) {
            source = repository.findByBusinessIdAndApprovalStatus(businessId, approvalStatus, pageable);
        } else {
            source = repository.findByBusinessId(businessId, pageable);
        }

        if (deletionRequestStatus != null) {
            source = source.stream()
                    .filter(p -> p.getDeletionRequestStatus() == deletionRequestStatus)
                    .toList();
        }

        if (q != null && !q.isBlank()) {
            String query = q.toLowerCase(Locale.ROOT);
            source = source.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(query))
                    .toList();
        }

        long total = source.size();

        return PagedResponse.<Package>builder()
                .items(source)
                .page(page)
                .size(size)
                .total(total)
                .build();
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

    public List<Package> getAllApprovedPackages() {
        return repository.findByApprovalStatus(ApprovalStatus.APPROVED.toString());
    }

    public List<Package> getAllApprovedPackages(int page, int size) {
        validatePagination(page, size);
        return repository.findByApprovalStatus(
                ApprovalStatus.APPROVED.toString(),
                PageRequest.of(page, size)
        );
    }

    public long countAllApprovedPackages() {
        return repository.countByApprovalStatus(ApprovalStatus.APPROVED.toString());
    }

    public PagedResponse<Package> getDeletionRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Package> items = repository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING, pageable);
        long total = repository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING).size();

        return PagedResponse.<Package>builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .build();
    }

    public List<Package> getApprovedByBusiness(String businessId) {
        return repository.findByBusinessIdAndApprovalStatus(businessId, ApprovalStatus.APPROVED.toString());
    }

    public Package getApprovedDetail(String packageId) {
        return repository.findByIdAndApprovalStatus(packageId, ApprovalStatus.APPROVED.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
    }

    public PackageDetailResponse getApprovedDetailWithGuide(String packageId) {
        Package pkg = getApprovedDetail(packageId);
        return toPackageDetailResponse(pkg);
    }

    public Package getPackageDetail(String packageId) {
        return repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
    }

    public Package assignGuideToPackage(String packageId, String guideId) {
        if (guideId == null || guideId.isBlank()) {
            throw new ValidationException("guideId is required");
        }

        Package pkg = repository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        User guide = userRepository.findByIdAndRole(guideId, "GUIDE")
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        if (!ApprovalStatus.APPROVED.name().equals(guide.getStatus())) {
            throw new ValidationException("Only approved guide can be assigned");
        }

        GuideProfile profile = guide.getGuideProfile();

        pkg.setGuideId(guide.getId());
        pkg.setGuideName(profile != null && profile.getFullName() != null
                ? profile.getFullName()
                : guide.getUsername());
        pkg.setUpdatedAt(LocalDateTime.now());

        return repository.save(pkg);
    }

    private PackageDetailResponse toPackageDetailResponse(Package pkg) {
        if (pkg.getGuideId() == null || pkg.getGuideId().isBlank()) {
            return PackageDetailResponse.builder()
                    .packageData(pkg)
                    .guide(null)
                    .build();
        }

        User guide = userRepository.findByIdAndRole(pkg.getGuideId(), "GUIDE").orElse(null);

        GuideSummaryResponse guideSummary = null;
        if (guide != null) {
            GuideProfile profile = guide.getGuideProfile();
            guideSummary = GuideSummaryResponse.builder()
                    .id(guide.getId())
                    .fullName(profile != null && profile.getFullName() != null ? profile.getFullName() : pkg.getGuideName())
                    .phone(profile != null ? profile.getPhone() : null)
                    .status(guide.getStatus())
                    .build();
        }

        return PackageDetailResponse.builder()
                .packageData(pkg)
                .guide(guideSummary)
                .build();
    }

    private void validateBusinessOwnership(User vendor, String businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new AccessDeniedException("Unauthorized access");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new ValidationException("size must be greater than 0");
        }
    }
}
