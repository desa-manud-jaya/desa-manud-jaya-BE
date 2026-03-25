package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.PackageCategory;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendorPackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private VendorPackageService vendorPackageService;

    private User vendor;
    private Business business;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        vendor = User.builder().id("vendor-1").username("vendor1").role("VENDOR").build();
        business = Business.builder().id("business-1").vendorId("vendor-1").build();
    }

    @Test
    void createPackageSuccess() {
        CreatePackageRequest request = new CreatePackageRequest(
                "Package A",
                PackageCategory.NATURE,
                BigDecimal.valueOf(100_000),
                2,
                20,
                List.of("Day 1"),
                List.of("Guide"),
                null,
                null,
                null
        );

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Package result = vendorPackageService.createPackage(
                request,
                "business-1",
                "vendor1",
                "https://doc",
                "https://photo"
        );

        assertEquals("vendor-1", result.getVendorId());
        assertEquals("PENDING", result.getApprovalStatus());
    }

    @Test
    void createPackageUnauthorizedThrows() {
        User other = User.builder().id("vendor-2").username("vendor2").build();

        when(userRepository.findByUsername("vendor2")).thenReturn(Optional.of(other));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));

        assertThrows(RuntimeException.class, () ->
                vendorPackageService.createPackage(new CreatePackageRequest(), "business-1", "vendor2", "doc", "photo")
        );
    }

    @Test
    void rejectRequiresReason() {
        assertThrows(RuntimeException.class, () -> vendorPackageService.reject("p-1", " "));
    }

    @Test
    void rejectSetsStatusAndReason() {
        Package pkg = Package.builder().id("p-1").approvalStatus("PENDING").build();
        when(packageRepository.findById("p-1")).thenReturn(Optional.of(pkg));

        vendorPackageService.reject("p-1", "Document is incomplete");

        assertEquals("REJECTED", pkg.getApprovalStatus());
        assertEquals("Document is incomplete", pkg.getRejectionReason());
        verify(packageRepository, times(1)).save(pkg);
    }

    @Test
    void getApprovedByBusinessOnlyApprovedStatusQuery() {
        when(packageRepository.findByBusinessIdAndApprovalStatus("business-1", ApprovalStatus.APPROVED.toString()))
                .thenReturn(List.of(Package.builder().id("p-1").approvalStatus("APPROVED").build()));

        List<Package> result = vendorPackageService.getApprovedByBusiness("business-1");

        assertEquals(1, result.size());
    }

    @Test
    void getApprovedDetailThrowsWhenNotApproved() {
        when(packageRepository.findByIdAndApprovalStatus("p-1", ApprovalStatus.APPROVED.toString()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vendorPackageService.getApprovedDetail("p-1"));
    }

    @Test
    void requestDeletionSetsPendingStatus() {
        Package pkg = Package.builder().id("p-1").businessId("business-1").deletionRequestStatus(com.example.manud_jaya.model.dto.DeletionRequestStatus.NONE).build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        when(packageRepository.findById("p-1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenAnswer(inv -> inv.getArgument(0));

        Package saved = vendorPackageService.requestDeletion("business-1", "p-1", "vendor1", "No longer offered");

        assertEquals(com.example.manud_jaya.model.dto.DeletionRequestStatus.PENDING, saved.getDeletionRequestStatus());
        assertEquals("No longer offered", saved.getDeletionRequestReason());
    }

    @Test
    void approveDeletionRequiresPendingRequest() {
        Package pkg = Package.builder().id("p-1").deletionRequestStatus(com.example.manud_jaya.model.dto.DeletionRequestStatus.NONE).build();
        when(packageRepository.findById("p-1")).thenReturn(Optional.of(pkg));

        assertThrows(RuntimeException.class, () -> vendorPackageService.approveDeletionRequest("p-1", "admin-1", "ok"));
    }
}
