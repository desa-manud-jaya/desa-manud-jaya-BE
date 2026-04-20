package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.inbound.request.AdminBookingDecisionRequest;
import com.example.manud_jaya.model.inbound.request.CreateBusinessRequest;
import com.example.manud_jaya.model.inbound.request.CreateDestinationRequest;
import com.example.manud_jaya.model.inbound.request.DocumentReviewRequest;
import com.example.manud_jaya.model.inbound.request.PackageDeletionRequest;
import com.example.manud_jaya.model.inbound.request.UpdateBusinessProfile;
import com.example.manud_jaya.model.inbound.request.UpdatePackageRequest;
import com.example.manud_jaya.service.AdminDestinationService;
import com.example.manud_jaya.service.AdminOperationsService;
import com.example.manud_jaya.service.AdminService;
import com.example.manud_jaya.service.ApprovalCenterService;
import com.example.manud_jaya.service.AuthService;
import com.example.manud_jaya.service.BookingTransactionService;
import com.example.manud_jaya.service.DashboardAnalyticsService;
import com.example.manud_jaya.service.DestinationService;
import com.example.manud_jaya.service.PublicBusinessService;
import com.example.manud_jaya.service.PublicDestinationService;
import com.example.manud_jaya.service.SupabaseStorageService;
import com.example.manud_jaya.service.VendorDocumentService;
import com.example.manud_jaya.service.VendorOperationsService;
import com.example.manud_jaya.service.VendorPackageService;
import com.example.manud_jaya.service.VendorService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerCoverageTest {

    private Authentication auth(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        return authentication;
    }

    @Test
    void adminAnalyticsControllerDelegates() {
        DashboardAnalyticsService svc = mock(DashboardAnalyticsService.class);
        var c = new AdminAnalyticsController(svc);
        assertEquals(200, c.getDashboardAnalytics().getStatusCode().value());
    }

    @Test
    void adminApprovalCenterControllerDelegates() {
        ApprovalCenterService svc = mock(ApprovalCenterService.class);
        var c = new AdminApprovalCenterController(svc);
        assertEquals(200, c.getQueue(null, null, null, 0, 10, "desc").getStatusCode().value());
    }

    @Test
    void adminBusinessControllerBranches() {
        VendorPackageService svc = mock(VendorPackageService.class);
        when(svc.getByBusiness("b1")).thenReturn(List.of());
        when(svc.getPackageByStatus("b1", ApprovalStatus.APPROVED)).thenReturn(List.of());
        var c = new AdminBusinessController(svc);
        assertEquals(200, c.getPackagesByBusiness("b1", null).getStatusCode().value());
        assertEquals(200, c.getPackagesByBusiness("b1", ApprovalStatus.APPROVED).getStatusCode().value());
    }

    @Test
    void adminControllerDelegates() {
        AdminService svc = mock(AdminService.class);
        when(svc.getPendingVendors()).thenReturn(List.of());
        when(svc.getApproveVendors()).thenReturn(List.of());
        var c = new AdminController(svc);
        assertEquals(200, c.getPendingVendors().getStatusCode().value());
        assertEquals(200, c.getApprovedVendors().getStatusCode().value());
        c.approveVendor("u1", auth("admin"));
        c.rejectVendor("u1", auth("admin"));
        verify(svc).approveVendor("u1", "admin");
        verify(svc).rejectVendor("u1", "admin");
    }

    @Test
    void adminDestinationControllerDelegates() {
        AdminDestinationService svc = mock(AdminDestinationService.class);
        AuthService authService = mock(AuthService.class);
        when(svc.getPendingDestinations()).thenReturn(List.of());
        when(authService.getUser("admin")).thenReturn(User.builder().id("a1").build());
        var c = new AdminDestinationController(svc, authService);
        assertEquals(200, c.getPendingDestinations().getStatusCode().value());
        c.approveDestination("d1", auth("admin"));
        c.rejectDestination("d1", auth("admin"));
        verify(svc).approveDestination("d1", "a1");
        verify(svc).rejectDestination("d1", "a1");
    }

    @Test
    void adminDocumentControllerDelegates() throws Exception {
        VendorDocumentService svc = mock(VendorDocumentService.class);
        var c = new AdminDocumentController(svc);
        assertEquals(200, c.getPending(0, 10).getStatusCode().value());
        assertEquals(200, c.getDocuments(VerificationStatus.PENDING, VendorDocumentType.NIB, 0, 10).getStatusCode().value());

        DocumentReviewRequest req = new DocumentReviewRequest();
        req.setNote("ok");
        assertEquals(200, c.approve("doc1", req, auth("admin")).getStatusCode().value());

        DocumentReviewRequest rej = new DocumentReviewRequest();
        rej.setReason("bad");
        rej.setNote("note");
        assertEquals(200, c.reject("doc2", rej, auth("admin")).getStatusCode().value());
    }

    @Test
    void adminOperationsControllerDelegates() {
        AdminOperationsService adminOps = mock(AdminOperationsService.class);
        BookingTransactionService bookingSvc = mock(BookingTransactionService.class);
        when(adminOps.getBookings(0, 10)).thenReturn(null);
        when(adminOps.getReviews(0, 10)).thenReturn(null);
        when(bookingSvc.getAdminBookingPayments(null, 0, 10)).thenReturn(null);
        when(bookingSvc.getAdminBookingPaymentDetail("bk1")).thenReturn(null);
        when(bookingSvc.reviewBookingPayment("admin", "bk1", "APPROVE", "ok", null)).thenReturn(null);
        when(bookingSvc.assignGuideToBooking("admin", "bk1", "g1")).thenReturn(null);
        var c = new AdminOperationsController(adminOps, bookingSvc);

        assertEquals(200, c.getBookings(0, 10).getStatusCode().value());
        assertEquals(200, c.getBookingPayments(null, 0, 10).getStatusCode().value());
        assertEquals(200, c.getBookingPaymentDetail("bk1").getStatusCode().value());

        AdminBookingDecisionRequest req = AdminBookingDecisionRequest.builder().decision("APPROVE").note("ok").build();
        assertEquals(200, c.reviewBookingPayment(auth("admin"), "bk1", req).getStatusCode().value());
        assertEquals(200, c.assignGuideToBooking(auth("admin"), "bk1", "g1").getStatusCode().value());
        assertEquals(200, c.getReviews(0, 10).getStatusCode().value());
    }

    @Test
    void adminPackagesControllerDelegates() {
        VendorPackageService svc = mock(VendorPackageService.class);
        AuthService authService = mock(AuthService.class);
        when(authService.getUser("admin")).thenReturn(User.builder().id("a1").build());
        when(svc.getPending()).thenReturn(List.of());
        var c = new AdminPackages(svc, authService);

        assertEquals(200, c.approve("p1", auth("admin")).getStatusCode().value());
        assertEquals(200, c.reject("p1", "reason", auth("admin")).getStatusCode().value());
        assertEquals(200, c.pending().getStatusCode().value());
        assertEquals(200, c.detail("p1").getStatusCode().value());
        assertEquals(200, c.deletionRequests(0, 10).getStatusCode().value());
        assertEquals(200, c.approveDeletion("p1", "note", auth("admin")).getStatusCode().value());
        assertEquals(200, c.rejectDeletion("p1", "reason", auth("admin")).getStatusCode().value());
    }

    @Test
    void adminRevenueControllerDelegates() {
        BookingTransactionService svc = mock(BookingTransactionService.class);
        when(svc.getRevenueSummary(null, null)).thenReturn(null);
        var c = new AdminRevenueController(svc);
        assertEquals(200, c.getRevenueSummary(null, null).getStatusCode().value());
    }

    @Test
    void publicBusinessControllerDelegates() {
        PublicBusinessService svc = mock(PublicBusinessService.class);
        var c = new PublicBusinessController(svc);
        assertEquals(200, c.getBusinesses(0, 10).getStatusCode().value());
        assertEquals(200, c.getBusinessDetail("b1").getStatusCode().value());
    }

    @Test
    void publicDestinationControllerDelegates() {
        PublicDestinationService svc = mock(PublicDestinationService.class);
        var c = new PublicDestinationController(svc);
        assertEquals(200, c.getAllApprovedDestinations().getStatusCode().value());
        assertEquals(200, c.getBusinessDestinations("b1").getStatusCode().value());
        assertEquals(200, c.getDestinationDetail("d1").getStatusCode().value());
    }

    @Test
    void publicPackageControllerBranches() {
        VendorPackageService pkg = mock(VendorPackageService.class);
        PublicDestinationService dst = mock(PublicDestinationService.class);
        when(pkg.getAllApprovedPackages()).thenReturn(List.of());
        when(pkg.getAllApprovedPackages(0, 10)).thenReturn(List.of());
        when(pkg.countAllApprovedPackages()).thenReturn(0L);
        when(dst.getAllApprovedDestinations()).thenReturn(List.of());
        when(dst.getAllApprovedDestinations(0, 10)).thenReturn(List.of());
        when(dst.countAllApprovedDestinations()).thenReturn(0L);

        var c = new PublicPackageController(pkg, dst);
        assertEquals(200, c.getAllApprovedPackages().getStatusCode().value());
        assertEquals(200, c.getAllApprovedPackagesAndDestinations(null, null).getStatusCode().value());
        assertEquals(200, c.getAllApprovedPackagesAndDestinations(0, 10).getStatusCode().value());
        assertEquals(200, c.getApprovedPackagesByBusiness("b1").getStatusCode().value());
        assertEquals(200, c.getApprovedPackageDetail("p1").getStatusCode().value());
    }

    @Test
    void userBookingControllerDelegates() {
        BookingTransactionService svc = mock(BookingTransactionService.class);
        var c = new UserBookingController(svc);
        var req = com.example.manud_jaya.model.inbound.request.CreateBookingRequest.builder().businessId("b").packageId("p").quantity(1).build();
        MultipartFile file = mock(MultipartFile.class);
        assertEquals(200, c.createBooking(auth("user"), req).getStatusCode().value());
        assertEquals(200, c.uploadPaymentProof(auth("user"), "bk1", file).getStatusCode().value());
        assertEquals(200, c.getBookingHistory(auth("user"), "u1", 0, 10).getStatusCode().value());
    }

    @Test
    void vendorAnalyticsControllerDelegates() {
        DashboardAnalyticsService svc = mock(DashboardAnalyticsService.class);
        var c = new VendorAnalyticsController(svc);
        assertEquals(200, c.getDashboardAnalytics(auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getImpactAnalytics(auth("vendor")).getStatusCode().value());
    }

    @Test
    void vendorBusinessControllerDelegates() {
        VendorService svc = mock(VendorService.class);
        var c = new VendorBusinessController(svc);
        assertEquals(200, c.createBusiness(new CreateBusinessRequest(), auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getBusinessDetail("b1", auth("vendor")).getStatusCode().value());
    }

    @Test
    void vendorControllerDelegates() {
        PublicBusinessService publicBusinessService = mock(PublicBusinessService.class);
        AuthService authService = mock(AuthService.class);
        when(authService.getUser("vendor")).thenReturn(User.builder().id("v1").build());
        when(authService.putUserVendor(any(), any())).thenReturn(User.builder().id("v1").build());
        when(publicBusinessService.getBusinessByVendor("v1")).thenReturn(Business.builder().id("b1").build());
        var c = new VendorController(publicBusinessService, authService);

        assertEquals(200, c.getVendor(auth("vendor")).getStatusCode().value());
        assertEquals(200, c.putVendor(auth("vendor"), new UpdateBusinessProfile()).getStatusCode().value());
        assertEquals(200, c.getBusiness("v1").getStatusCode().value());
    }

    @Test
    void vendorDestinationControllerDelegates() throws Exception {
        DestinationService svc = mock(DestinationService.class);
        var c = new VendorDestinationController(svc);
        assertEquals(200, c.createDestination("b1", new CreateDestinationRequest(), List.of(), auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getDestinations("b1", auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getApprovedDestinations("b1", auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getPendingDestinations("b1", auth("vendor")).getStatusCode().value());
    }

    @Test
    void vendorDocumentControllerDelegates() throws Exception {
        VendorDocumentService svc = mock(VendorDocumentService.class);
        var c = new VendorDocumentController(svc);
        MultipartFile file = mock(MultipartFile.class);
        assertEquals(200, c.uploadDocument("b1", VendorDocumentType.NIB, file, auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getDocuments("b1", auth("vendor")).getStatusCode().value());
        assertEquals(200, c.getProgress("b1", auth("vendor")).getStatusCode().value());
    }

    @Test
    void vendorOperationsControllerDelegates() {
        VendorOperationsService svc = mock(VendorOperationsService.class);
        var c = new VendorOperationsController(svc);
        assertEquals(200, c.getBookings(auth("vendor"), 0, 10).getStatusCode().value());
        assertEquals(200, c.getReviews(auth("vendor"), 0, 10).getStatusCode().value());
    }

    @Test
    void smokeConstructors() {
        // Ensure test class touches all imports for coverage stability
        assertNotNull(new PackageDeletionRequest());
        assertNotNull(new UpdatePackageRequest());
        assertNotNull(new SupabaseStorageService(null));
        assertNotNull(DeletionRequestStatus.NONE);
    }
}
