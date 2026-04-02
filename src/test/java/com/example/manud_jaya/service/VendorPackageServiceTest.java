package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.PackageCategory;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.model.inbound.request.UpdatePackageRequest;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        Package result = vendorPackageService.createPackage(request, "business-1", "vendor1", "https://doc", "https://photo");

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
    void updatePackageSuccess() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        Package pkg = Package.builder().id("p1").businessId("business-1").name("Old").build();
        when(packageRepository.findById("p1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePackageRequest req = new UpdatePackageRequest();
        req.setName("New");

        Package saved = vendorPackageService.updatePackage("business-1", "p1", "vendor1", req);
        assertEquals("New", saved.getName());
    }

    @Test
    void updatePackagePackageNotInBusinessThrows() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        when(packageRepository.findById("p1")).thenReturn(Optional.of(Package.builder().id("p1").businessId("other").build()));

        assertThrows(RuntimeException.class, () -> vendorPackageService.updatePackage("business-1", "p1", "vendor1", new UpdatePackageRequest()));
    }

    @Test
    void requestDeletionBranches() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        Package pkg = Package.builder().id("p-1").businessId("business-1").deletionRequestStatus(DeletionRequestStatus.NONE).build();
        when(packageRepository.findById("p-1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenAnswer(inv -> inv.getArgument(0));

        Package saved = vendorPackageService.requestDeletion("business-1", "p-1", "vendor1", "No longer offered");
        assertEquals(DeletionRequestStatus.PENDING, saved.getDeletionRequestStatus());

        assertThrows(RuntimeException.class, () -> vendorPackageService.requestDeletion("business-1", "p-1", "vendor1", " "));
    }

    @Test
    void approveAndRejectShouldUpdateStatus() {
        Package pkg = Package.builder().id("p1").build();
        when(packageRepository.findById("p1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenAnswer(i -> i.getArgument(0));

        vendorPackageService.approve("p1", "admin", "note");
        assertEquals("APPROVED", pkg.getApprovalStatus());

        vendorPackageService.reject("p1", "reason", "admin");
        assertEquals("REJECTED", pkg.getApprovalStatus());
    }

    @Test
    void rejectRequiresReason() {
        assertThrows(RuntimeException.class, () -> vendorPackageService.reject("p-1", " "));
    }

    @Test
    void approveDeletionAndRejectDeletion() {
        Package pkg = Package.builder().id("p1").deletionRequestStatus(DeletionRequestStatus.PENDING).build();
        when(packageRepository.findById("p1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenAnswer(i -> i.getArgument(0));

        Package approved = vendorPackageService.approveDeletionRequest("p1", "admin", "ok");
        assertEquals(DeletionRequestStatus.APPROVED, approved.getDeletionRequestStatus());

        pkg.setDeletionRequestStatus(DeletionRequestStatus.PENDING);
        Package rejected = vendorPackageService.rejectDeletionRequest("p1", "admin", "bad");
        assertEquals(DeletionRequestStatus.REJECTED, rejected.getDeletionRequestStatus());

        pkg.setDeletionRequestStatus(DeletionRequestStatus.NONE);
        assertThrows(RuntimeException.class, () -> vendorPackageService.approveDeletionRequest("p1", "admin", "ok"));
        assertThrows(RuntimeException.class, () -> vendorPackageService.rejectDeletionRequest("p1", "admin", "bad"));
        assertThrows(RuntimeException.class, () -> vendorPackageService.rejectDeletionRequest("p1", "admin", " "));
    }

    @Test
    void getByBusinessForVendorShouldValidateOwnership() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));
        when(packageRepository.findByBusinessId("business-1")).thenReturn(List.of(Package.builder().id("p1").build()));

        assertEquals(1, vendorPackageService.getByBusinessForVendor("business-1", "vendor1").size());
    }

    @Test
    void getByBusinessForVendorPagedShouldHandleFilters() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("business-1")).thenReturn(Optional.of(business));

        when(packageRepository.findByBusinessId("business-1", org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(List.of(Package.builder().id("p1").name("Nature A").deletionRequestStatus(DeletionRequestStatus.PENDING).build()));
        when(packageRepository.findByBusinessIdAndApprovalStatus("business-1", "APPROVED", org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(List.of(Package.builder().id("p2").name("Approved").deletionRequestStatus(DeletionRequestStatus.NONE).build()));

        PagedResponse<Package> noStatus = vendorPackageService.getByBusinessForVendor("business-1", "vendor1", null, null, null, 0, 10);
        assertEquals(1, noStatus.getItems().size());

        PagedResponse<Package> withStatus = vendorPackageService.getByBusinessForVendor("business-1", "vendor1", "APPROVED", null, null, 0, 10);
        assertEquals(1, withStatus.getItems().size());

        PagedResponse<Package> filtered = vendorPackageService.getByBusinessForVendor("business-1", "vendor1", null, DeletionRequestStatus.PENDING, "nature", 0, 10);
        assertEquals(1, filtered.getItems().size());
    }

    @Test
    void getSimpleQueryMethodsShouldDelegate() {
        when(packageRepository.findByBusinessIdAndApprovalStatus("business-1", ApprovalStatus.APPROVED.toString()))
                .thenReturn(List.of(Package.builder().id("p1").build()));
        when(packageRepository.findByBusinessId("business-1")).thenReturn(List.of(Package.builder().id("p2").build()));
        when(packageRepository.findByApprovalStatus(ApprovalStatus.PENDING.toString())).thenReturn(List.of(Package.builder().id("p3").build()));
        when(packageRepository.findByApprovalStatus(ApprovalStatus.APPROVED.toString())).thenReturn(List.of(Package.builder().id("p4").build()));
        when(packageRepository.findByApprovalStatus(ApprovalStatus.APPROVED.toString(), org.springframework.data.domain.PageRequest.of(0, 10))).thenReturn(List.of(Package.builder().id("p5").build()));
        when(packageRepository.countByApprovalStatus(ApprovalStatus.APPROVED.toString())).thenReturn(1L);
        when(packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 10))).thenReturn(List.of(Package.builder().id("p6").build()));
        when(packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING)).thenReturn(List.of(Package.builder().id("p6").build()));
        when(packageRepository.findByIdAndApprovalStatus("p1", ApprovalStatus.APPROVED.toString())).thenReturn(Optional.of(Package.builder().id("p1").build()));
        when(packageRepository.findById("p1")).thenReturn(Optional.of(Package.builder().id("p1").build()));

        assertEquals(1, vendorPackageService.getPackageByStatus("business-1", ApprovalStatus.APPROVED).size());
        assertEquals(1, vendorPackageService.getByBusiness("business-1").size());
        assertEquals(1, vendorPackageService.getPending().size());
        assertEquals(1, vendorPackageService.getAllApprovedPackages().size());
        assertEquals(1, vendorPackageService.getAllApprovedPackages(0, 10).size());
        assertEquals(1L, vendorPackageService.countAllApprovedPackages());
        assertEquals(1, vendorPackageService.getDeletionRequests(0, 10).getTotal());
        assertEquals(1, vendorPackageService.getApprovedByBusiness("business-1").size());
        assertEquals("p1", vendorPackageService.getApprovedDetail("p1").getId());
        assertEquals("p1", vendorPackageService.getPackageDetail("p1").getId());
    }

    @Test
    void getApprovedDetailThrowsWhenNotApproved() {
        when(packageRepository.findByIdAndApprovalStatus("p-1", ApprovalStatus.APPROVED.toString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vendorPackageService.getApprovedDetail("p-1"));
    }

    @Test
    void paginationValidationShouldThrow() {
        assertThrows(RuntimeException.class, () -> vendorPackageService.getAllApprovedPackages(-1, 10));
        assertThrows(RuntimeException.class, () -> vendorPackageService.getAllApprovedPackages(0, 0));
    }
}
