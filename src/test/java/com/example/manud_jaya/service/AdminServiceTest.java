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
import com.example.manud_jaya.repository.BookingTransactionRepository;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BookingTransactionRepository bookingTransactionRepository;

    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private AdminService adminService;

    private User pendingVendor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(adminService, "moderationAuditService", moderationAuditService);

        VendorProfile pendingProfile = VendorProfile.builder()
                .vendorName("Pending Vendor")
                .description("A pending vendor")
                .phone("1234567890")
                .address("123 Main St")
                .ktpNumber("1234567890123456")
                .approvalStatus("PENDING")
                .build();

        pendingVendor = User.builder()
                .id("vendor-1")
                .username("vendor1")
                .email("vendor1@test.com")
                .role("VENDOR")
                .status("PENDING")
                .vendorProfile(pendingProfile)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getPendingVendorsSuccess() {
        when(userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", ApprovalStatus.PENDING))
                .thenReturn(List.of(pendingVendor));

        List<VendorPendingResponse> result = adminService.getPendingVendors();

        assertEquals(1, result.size());
        assertEquals("Pending Vendor", result.get(0).getVendorName());
    }

    @Test
    void getApprovedVendorsSuccess() {
        User approvedVendor = User.builder()
                .id("vendor-2")
                .username("vendor2")
                .email("vendor2@test.com")
                .role("VENDOR")
                .status("ACTIVE")
                .vendorProfile(VendorProfile.builder()
                        .vendorName("Approved Vendor")
                        .phone("081")
                        .address("Addr")
                        .ktpNumber("111")
                        .approvalStatus("APPROVED")
                        .build())
                .build();

        when(userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", ApprovalStatus.APPROVED))
                .thenReturn(List.of(approvedVendor));

        List<VendorPendingResponse> result = adminService.getApproveVendors();

        assertEquals(1, result.size());
        assertEquals("Approved Vendor", result.get(0).getVendorName());
    }

    @Test
    void approveVendorSuccessCreatesBusinessWhenMissing() {
        when(userRepository.findById("vendor-1")).thenReturn(Optional.of(pendingVendor));
        when(businessRepository.findFirstByVendorId("vendor-1")).thenReturn(Optional.empty());
        when(businessRepository.save(any(Business.class))).thenAnswer(invocation -> {
            Business business = invocation.getArgument(0);
            business.setId("business-1");
            return business;
        });

        adminService.approveVendor("vendor-1", "admin-1");

        assertEquals("APPROVED", pendingVendor.getVendorProfile().getApprovalStatus());
        assertEquals("ACTIVE", pendingVendor.getStatus());
        assertNotNull(pendingVendor.getVendorProfile().getApprovedAt());
        assertEquals("business-1", pendingVendor.getVendorProfile().getBusinessId());

        verify(businessRepository, times(1)).save(argThat((Business business) ->
                business.getVendorId().equals("vendor-1")
                        && business.getApprovalStatus().equals("APPROVED")
        ));
        verify(userRepository, times(1)).save(pendingVendor);
        verify(moderationAuditService).log("VENDOR", "APPROVE", "admin-1", "vendor-1", "Vendor approved");
    }

    @Test
    void approveVendorSkipsBusinessCreationWhenAlreadyExists() {
        when(userRepository.findById("vendor-1")).thenReturn(Optional.of(pendingVendor));
        when(businessRepository.findFirstByVendorId("vendor-1"))
                .thenReturn(Optional.of(Business.builder().id("b-1").vendorId("vendor-1").build()));

        adminService.approveVendor("vendor-1", "admin-1");

        assertEquals("b-1", pendingVendor.getVendorProfile().getBusinessId());
        verify(businessRepository, never()).save(any(Business.class));
        verify(userRepository, times(1)).save(pendingVendor);
    }

    @Test
    void rejectVendorSuccess() {
        when(userRepository.findById("vendor-1")).thenReturn(Optional.of(pendingVendor));

        adminService.rejectVendor("vendor-1", "admin-1");

        assertEquals("REJECTED", pendingVendor.getVendorProfile().getApprovalStatus());
        assertEquals("INACTIVE", pendingVendor.getStatus());
        verify(userRepository, times(1)).save(pendingVendor);
        verify(moderationAuditService).log("VENDOR", "REJECT", "admin-1", "vendor-1", "Vendor rejected");
    }

    @Test
    void approveVendorNotFoundThrows() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.approveVendor("missing", "admin-1"));
    }

    @Test
    void rejectVendorNotFoundThrows() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.rejectVendor("missing", "admin-1"));
    }

    @Test
    void getPendingGuidesAndApprovedGuidesSuccess() {
        User pendingGuide = User.builder()
                .id("guide-1")
                .username("guide1")
                .email("guide1@mail.com")
                .role("GUIDE")
                .status("PENDING")
                .guideProfile(GuideProfile.builder().fullName("Guide One").phone("080").licenseNumber("L-1").cvDocumentUrl("https://example/cv-1.pdf").build())
                .build();
        User approvedGuide = User.builder()
                .id("guide-2")
                .username("guide2")
                .email("guide2@mail.com")
                .role("GUIDE")
                .status("APPROVED")
                .guideProfile(GuideProfile.builder().fullName("Guide Two").phone("081").licenseNumber("L-2").cvDocumentUrl("https://example/cv-2.pdf").build())
                .build();

        when(userRepository.findByRoleAndStatus("GUIDE", "PENDING")).thenReturn(List.of(pendingGuide));
        when(userRepository.findByRoleAndStatus("GUIDE", "APPROVED")).thenReturn(List.of(approvedGuide));

        List<GuidePendingResponse> pending = adminService.getPendingGuides();
        List<GuidePendingResponse> approved = adminService.getApprovedGuides();

        assertEquals(1, pending.size());
        assertEquals("Guide One", pending.get(0).getFullName());
        assertEquals("https://example/cv-1.pdf", pending.get(0).getCvDocumentUrl());
        assertEquals(1, approved.size());
        assertEquals("Guide Two", approved.get(0).getFullName());
        assertEquals("https://example/cv-2.pdf", approved.get(0).getCvDocumentUrl());
    }

    @Test
    void getApprovedGuidesWithTripDateShouldReturnOnlyAvailableGuides() {
        User guide1 = User.builder()
                .id("guide-1")
                .username("guide1")
                .email("guide1@mail.com")
                .role("GUIDE")
                .status("APPROVED")
                .guideProfile(GuideProfile.builder().fullName("Guide One").build())
                .build();
        User guide2 = User.builder()
                .id("guide-2")
                .username("guide2")
                .email("guide2@mail.com")
                .role("GUIDE")
                .status("APPROVED")
                .guideProfile(GuideProfile.builder().fullName("Guide Two").build())
                .build();

        when(userRepository.findByRoleAndStatus("GUIDE", "APPROVED")).thenReturn(List.of(guide1, guide2));
        when(bookingTransactionRepository.existsByGuideIdAndTripDateAndStatusIn(eq("guide-1"), eq(LocalDate.parse("2026-05-01")), anyList()))
                .thenReturn(true);
        when(bookingTransactionRepository.existsByGuideIdAndTripDateAndStatusIn(eq("guide-2"), eq(LocalDate.parse("2026-05-01")), anyList()))
                .thenReturn(false);

        List<GuidePendingResponse> result = adminService.getApprovedGuides("2026-05-01");

        assertEquals(1, result.size());
        assertEquals("guide-2", result.get(0).getUserId());
    }

    @Test
    void getApprovedGuidesWithInvalidTripDateShouldThrowValidation() {
        assertThrows(ValidationException.class, () -> adminService.getApprovedGuides("01-05-2026"));
    }

    @Test
    void approveGuideSuccess() {
        User pendingGuide = User.builder()
                .id("guide-1")
                .role("GUIDE")
                .status("PENDING")
                .guideProfile(GuideProfile.builder().approvalStatus("PENDING").build())
                .build();

        when(userRepository.findByIdAndRole("guide-1", "GUIDE")).thenReturn(Optional.of(pendingGuide));

        adminService.approveGuide("guide-1", "admin-1");

        assertEquals("APPROVED", pendingGuide.getStatus());
        assertEquals("APPROVED", pendingGuide.getGuideProfile().getApprovalStatus());
        assertNotNull(pendingGuide.getGuideProfile().getApprovedAt());
        verify(userRepository).save(pendingGuide);
        verify(moderationAuditService).log("GUIDE", "APPROVE", "admin-1", "guide-1", "Guide approved");
    }

    @Test
    void rejectGuideSuccess() {
        User pendingGuide = User.builder()
                .id("guide-1")
                .role("GUIDE")
                .status("PENDING")
                .guideProfile(GuideProfile.builder().approvalStatus("PENDING").build())
                .build();

        when(userRepository.findByIdAndRole("guide-1", "GUIDE")).thenReturn(Optional.of(pendingGuide));

        adminService.rejectGuide("guide-1", "admin-1", "Not complete");

        assertEquals("REJECTED", pendingGuide.getStatus());
        assertEquals("REJECTED", pendingGuide.getGuideProfile().getApprovalStatus());
        assertEquals("Not complete", pendingGuide.getGuideProfile().getRejectionReason());
        verify(userRepository).save(pendingGuide);
        verify(moderationAuditService).log("GUIDE", "REJECT", "admin-1", "guide-1", "Not complete");
    }

    @Test
    void rejectGuideReasonMissingThrowsValidation() {
        assertThrows(ValidationException.class, () -> adminService.rejectGuide("guide-1", "admin-1", " "));
    }

    @Test
    void approveGuideNotFoundThrows() {
        when(userRepository.findByIdAndRole("guide-1", "GUIDE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.approveGuide("guide-1", "admin-1"));
    }

    @Test
    void rejectGuideNotFoundThrows() {
        when(userRepository.findByIdAndRole("guide-1", "GUIDE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.rejectGuide("guide-1", "admin-1", "Reason"));
    }
}
