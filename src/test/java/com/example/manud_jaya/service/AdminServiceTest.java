package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.VendorProfile;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.response.VendorPendingResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private AdminService adminService;

    private User pendingVendor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
    void approveVendorSuccessCreatesBusinessWhenMissing() {
        when(userRepository.findById("vendor-1")).thenReturn(Optional.of(pendingVendor));
        when(businessRepository.findFirstByVendorId("vendor-1")).thenReturn(Optional.empty());

        adminService.approveVendor("vendor-1", "admin-1");

        assertEquals("APPROVED", pendingVendor.getVendorProfile().getApprovalStatus());
        assertEquals("ACTIVE", pendingVendor.getStatus());
        assertNotNull(pendingVendor.getVendorProfile().getApprovedAt());

        verify(businessRepository, times(1)).save(argThat((Business business) ->
                business.getVendorId().equals("vendor-1")
                        && business.getApprovalStatus().equals("APPROVED")
        ));
        verify(userRepository, times(1)).save(pendingVendor);
    }

    @Test
    void approveVendorSkipsBusinessCreationWhenAlreadyExists() {
        when(userRepository.findById("vendor-1")).thenReturn(Optional.of(pendingVendor));
        when(businessRepository.findFirstByVendorId("vendor-1"))
                .thenReturn(Optional.of(Business.builder().id("b-1").vendorId("vendor-1").build()));

        adminService.approveVendor("vendor-1", "admin-1");

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
    }

    @Test
    void approveVendorNotFoundThrows() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.approveVendor("missing", "admin-1"));
    }
}
