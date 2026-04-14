package com.example.manud_jaya.service;

import com.example.manud_jaya.exception.ResourceNotFoundException;
import com.example.manud_jaya.exception.ValidationException;
import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.GuideProfile;
import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.GuidePendingResponse;
import com.example.manud_jaya.model.inbound.response.VendorPendingResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @Autowired(required = false)
    private ModerationAuditService moderationAuditService;

    public List<VendorPendingResponse> getPendingVendors() {

        List<User> vendors = userRepository
                .findByRoleAndVendorProfileApprovalStatus(
                        "VENDOR",
                        ApprovalStatus.PENDING
                );

        return vendors.stream()
                .map(user -> VendorPendingResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .vendorName(user.getVendorProfile().getVendorName())
                        .phone(user.getVendorProfile().getPhone())
                        .address(user.getVendorProfile().getAddress())
                        .ktpNumber(user.getVendorProfile().getKtpNumber())
                        .build())
                .toList();
    }

    public List<VendorPendingResponse> getApproveVendors() {

        List<User> vendors = userRepository
                .findByRoleAndVendorProfileApprovalStatus(
                        "VENDOR",
                        ApprovalStatus.APPROVED
                );

        return vendors.stream()
                .map(user -> VendorPendingResponse.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .vendorName(user.getVendorProfile().getVendorName())
                        .phone(user.getVendorProfile().getPhone())
                        .address(user.getVendorProfile().getAddress())
                        .ktpNumber(user.getVendorProfile().getKtpNumber())
                        .build())
                .toList();
    }

    public void approveVendor(String userId, String adminId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        VendorProfile profile = user.getVendorProfile();

        profile.setApprovalStatus(String.valueOf(ApprovalStatus.APPROVED));
        profile.setApprovedAt(LocalDateTime.now());

        user.setStatus("ACTIVE");
        user.setUpdatedAt(LocalDateTime.now());

        Business linkedBusiness = businessRepository.findFirstByVendorId(user.getId())
                .orElseGet(() -> {
                    Business business = Business.builder()
                            .vendorId(user.getId())
                            .name(profile.getVendorName())
                            .description(profile.getDescription())
                            .address(profile.getAddress())
                            .approvalStatus(String.valueOf(ApprovalStatus.APPROVED))
                            .createdAt(LocalDateTime.now())
                            .build();

                    return businessRepository.save(business);
                });

        profile.setBusinessId(linkedBusiness.getId());

        userRepository.save(user);

        if (moderationAuditService != null) {
            moderationAuditService.log("VENDOR", "APPROVE", adminId, user.getId(), "Vendor approved");
        }
    }

    public void rejectVendor(String userId, String adminId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        VendorProfile profile = user.getVendorProfile();

        profile.setApprovalStatus(String.valueOf(ApprovalStatus.REJECTED));
        profile.setApprovedAt(LocalDateTime.now());

        user.setStatus("INACTIVE");
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        if (moderationAuditService != null) {
            moderationAuditService.log("VENDOR", "REJECT", adminId, user.getId(), "Vendor rejected");
        }
    }

    public List<GuidePendingResponse> getPendingGuides() {
        return userRepository.findByRoleAndStatus("GUIDE", ApprovalStatus.PENDING.name())
                .stream()
                .map(this::toGuideResponse)
                .toList();
    }

    public List<GuidePendingResponse> getApprovedGuides() {
        return userRepository.findByRoleAndStatus("GUIDE", ApprovalStatus.APPROVED.name())
                .stream()
                .map(this::toGuideResponse)
                .toList();
    }

    public void approveGuide(String userId, String adminId) {
        User guide = userRepository.findByIdAndRole(userId, "GUIDE")
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        GuideProfile guideProfile = guide.getGuideProfile();
        if (guideProfile == null) {
            guideProfile = GuideProfile.builder().build();
            guide.setGuideProfile(guideProfile);
        }

        guideProfile.setApprovalStatus(ApprovalStatus.APPROVED.name());
        guideProfile.setApprovedAt(LocalDateTime.now());
        guideProfile.setRejectionReason(null);

        guide.setStatus(ApprovalStatus.APPROVED.name());
        guide.setUpdatedAt(LocalDateTime.now());
        userRepository.save(guide);

        if (moderationAuditService != null) {
            moderationAuditService.log("GUIDE", "APPROVE", adminId, guide.getId(), "Guide approved");
        }
    }

    public void rejectGuide(String userId, String adminId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Rejection reason is required");
        }

        User guide = userRepository.findByIdAndRole(userId, "GUIDE")
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        GuideProfile guideProfile = guide.getGuideProfile();
        if (guideProfile == null) {
            guideProfile = GuideProfile.builder().build();
            guide.setGuideProfile(guideProfile);
        }

        guideProfile.setApprovalStatus(ApprovalStatus.REJECTED.name());
        guideProfile.setApprovedAt(LocalDateTime.now());
        guideProfile.setRejectionReason(reason);

        guide.setStatus(ApprovalStatus.REJECTED.name());
        guide.setUpdatedAt(LocalDateTime.now());
        userRepository.save(guide);

        if (moderationAuditService != null) {
            moderationAuditService.log("GUIDE", "REJECT", adminId, guide.getId(), reason);
        }
    }

    private GuidePendingResponse toGuideResponse(User user) {
        GuideProfile profile = user.getGuideProfile();

        return GuidePendingResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(profile != null ? profile.getFullName() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .licenseNumber(profile != null ? profile.getLicenseNumber() : null)
                .status(user.getStatus())
                .build();
    }

}
