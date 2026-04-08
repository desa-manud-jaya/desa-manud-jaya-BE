package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.entity.VendorDocument;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ApprovalCenterServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private VendorDocumentRepository vendorDocumentRepository;

    @InjectMocks
    private ApprovalCenterService approvalCenterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getQueueReturnsMixedPendingRows() {
        User pendingVendor = User.builder().id("u-1").username("vendor").createdAt(LocalDateTime.now()).build();
        Package pendingPackage = Package.builder().id("p-1").name("Package").vendorId("u-1").approvalStatus("PENDING").createdAt(LocalDateTime.now()).build();
        Package deletionRequest = Package.builder().id("p-2").name("Package 2").vendorId("u-1").deletionRequestStatus(DeletionRequestStatus.PENDING).deletionRequestedAt(LocalDateTime.now()).build();
        VendorDocument pendingDoc = VendorDocument.builder().id("d-1").vendorId("u-1").documentType(VendorDocumentType.KTP).status(VerificationStatus.PENDING).submittedAt(LocalDateTime.now()).build();

        when(userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", "PENDING")).thenReturn(List.of(pendingVendor));
        when(packageRepository.findByApprovalStatus("PENDING")).thenReturn(List.of(pendingPackage));
        when(packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING)).thenReturn(List.of(deletionRequest));
        when(vendorDocumentRepository.findByStatus(VerificationStatus.PENDING)).thenReturn(List.of(pendingDoc));
        when(packageRepository.countByApprovalStatus("PENDING")).thenReturn(1L);
        when(vendorDocumentRepository.countByStatus(VerificationStatus.PENDING)).thenReturn(1L);

        var result = approvalCenterService.getQueue(null, null, null, 0, 10, "desc");

        assertEquals(4, result.getItems().size());
        assertEquals(4, result.getTotal());
    }
}
