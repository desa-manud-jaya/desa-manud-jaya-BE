package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.entity.VendorDocument;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.model.inbound.response.VendorDocumentProgressResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VendorDocumentServiceTest {

    @Mock
    private VendorDocumentRepository vendorDocumentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private SupabaseStorageService supabaseStorageService;
    @Mock
    private ModerationAuditService moderationAuditService;

    @InjectMocks
    private VendorDocumentService vendorDocumentService;

    private User vendor;
    private Business business;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vendor = User.builder().id("v-1").username("vendor1").build();
        business = Business.builder().id("b-1").vendorId("v-1").build();
    }

    @Test
    void uploadDocumentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "ktp.pdf", "application/pdf", "abc".getBytes());

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));
        when(supabaseStorageService.uploadDocument(any())).thenReturn("https://doc-url");
        when(vendorDocumentRepository.save(any(VendorDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        VendorDocument saved = vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1");

        assertEquals(VerificationStatus.PENDING, saved.getStatus());
        assertEquals("https://doc-url", saved.getFileUrl());
        verify(moderationAuditService).log("DOCUMENT", "SUBMIT", "v-1", saved.getId(), "Vendor submitted document");
    }

    @Test
    void uploadDocumentVendorNotFoundShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "ktp.pdf", "application/pdf", "abc".getBytes());
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1"));
    }

    @Test
    void uploadDocumentBusinessNotFoundShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "ktp.pdf", "application/pdf", "abc".getBytes());
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1"));
    }

    @Test
    void uploadDocumentUnauthorizedShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "ktp.pdf", "application/pdf", "abc".getBytes());
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(Business.builder().id("b-1").vendorId("other").build()));

        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1"));
    }

    @Test
    void uploadDocumentRejectsInvalidFiles() {
        MockMultipartFile empty = new MockMultipartFile("file", "ktp.pdf", "application/pdf", new byte[0]);
        MockMultipartFile large = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[11 * 1024 * 1024]);
        MockMultipartFile nullType = new MockMultipartFile("file", "x.pdf", null, "abc".getBytes());
        MockMultipartFile unsupported = new MockMultipartFile("file", "x.gif", "image/gif", "abc".getBytes());

        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, empty, "vendor1"));
        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, large, "vendor1"));
        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, nullType, "vendor1"));
        assertThrows(RuntimeException.class, () -> vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, unsupported, "vendor1"));
    }

    @Test
    void getVendorDocumentsShouldSortDescending() {
        VendorDocument older = VendorDocument.builder().id("1").submittedAt(LocalDateTime.now().minusDays(1)).build();
        VendorDocument newer = VendorDocument.builder().id("2").submittedAt(LocalDateTime.now()).build();

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));
        when(vendorDocumentRepository.findByVendorIdAndBusinessId("v-1", "b-1")).thenReturn(List.of(older, newer));

        List<VendorDocument> docs = vendorDocumentService.getVendorDocuments("b-1", "vendor1");
        assertEquals("2", docs.get(0).getId());
    }

    @Test
    void getVendorDocumentsUnauthorizedShouldThrow() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(Business.builder().id("b-1").vendorId("x").build()));
        assertThrows(RuntimeException.class, () -> vendorDocumentService.getVendorDocuments("b-1", "vendor1"));
    }

    @Test
    void getProgressShouldComputeCompletionRate() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));
        when(vendorDocumentRepository.findByVendorIdAndBusinessId("v-1", "b-1")).thenReturn(List.of(
                VendorDocument.builder().id("1").build(),
                VendorDocument.builder().id("2").build()
        ));
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.PENDING)).thenReturn(1L);
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.APPROVED)).thenReturn(1L);
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.REJECTED)).thenReturn(0L);

        VendorDocumentProgressResponse progress = vendorDocumentService.getProgress("b-1", "vendor1");
        assertEquals(50.0, progress.getCompletionRate());
    }

    @Test
    void getProgressTotalZeroShouldBeZeroRate() {
        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));
        when(vendorDocumentRepository.findByVendorIdAndBusinessId("v-1", "b-1")).thenReturn(List.of());
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.PENDING)).thenReturn(0L);
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.APPROVED)).thenReturn(0L);
        when(vendorDocumentRepository.countByBusinessIdAndStatus("b-1", VerificationStatus.REJECTED)).thenReturn(0L);

        VendorDocumentProgressResponse progress = vendorDocumentService.getProgress("b-1", "vendor1");
        assertEquals(0.0, progress.getCompletionRate());
    }

    @Test
    void getPendingDocumentsShouldReturnPaged() {
        when(vendorDocumentRepository.findByStatus(VerificationStatus.PENDING, PageRequest.of(0, 10))).thenReturn(List.of(VendorDocument.builder().id("1").build()));
        when(vendorDocumentRepository.countByStatus(VerificationStatus.PENDING)).thenReturn(1L);

        PagedResponse<VendorDocument> response = vendorDocumentService.getPendingDocuments(0, 10);
        assertEquals(1, response.getTotal());
    }

    @Test
    void getDocumentsShouldBranchByFilters() {
        when(vendorDocumentRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(VendorDocument.builder().id("1").build())));
        when(vendorDocumentRepository.count()).thenReturn(1L);

        PagedResponse<VendorDocument> noStatus = vendorDocumentService.getDocuments(null, null, 0, 10);
        assertEquals(1, noStatus.getTotal());

        when(vendorDocumentRepository.findByStatusAndDocumentType(VerificationStatus.PENDING, VendorDocumentType.KTP, PageRequest.of(0, 10)))
                .thenReturn(List.of(VendorDocument.builder().id("2").build()));
        PagedResponse<VendorDocument> withType = vendorDocumentService.getDocuments(VerificationStatus.PENDING, VendorDocumentType.KTP, 0, 10);
        assertEquals(1, withType.getTotal());

        when(vendorDocumentRepository.findByStatus(VerificationStatus.APPROVED, PageRequest.of(0, 10)))
                .thenReturn(List.of(VendorDocument.builder().id("3").build()));
        when(vendorDocumentRepository.countByStatus(VerificationStatus.APPROVED)).thenReturn(1L);
        PagedResponse<VendorDocument> statusOnly = vendorDocumentService.getDocuments(VerificationStatus.APPROVED, null, 0, 10);
        assertEquals(1, statusOnly.getTotal());
    }

    @Test
    void approveShouldSetFields() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("a1").build()));
        VendorDocument doc = VendorDocument.builder().id("doc1").status(VerificationStatus.PENDING).rejectionReason("x").build();
        when(vendorDocumentRepository.findById("doc1")).thenReturn(Optional.of(doc));
        when(vendorDocumentRepository.save(any(VendorDocument.class))).thenAnswer(i -> i.getArgument(0));

        VendorDocument result = vendorDocumentService.approve("doc1", "admin", "note");
        assertEquals(VerificationStatus.APPROVED, result.getStatus());
        assertEquals(null, result.getRejectionReason());
    }

    @Test
    void approveNotFoundBranchesShouldThrow() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vendorDocumentService.approve("doc1", "admin", "note"));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("a1").build()));
        when(vendorDocumentRepository.findById("doc1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vendorDocumentService.approve("doc1", "admin", "note"));
    }

    @Test
    void rejectShouldSetFieldsAndTrimReason() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("a1").build()));
        VendorDocument doc = VendorDocument.builder().id("doc1").status(VerificationStatus.PENDING).build();
        when(vendorDocumentRepository.findById("doc1")).thenReturn(Optional.of(doc));
        when(vendorDocumentRepository.save(any(VendorDocument.class))).thenAnswer(i -> i.getArgument(0));

        VendorDocument result = vendorDocumentService.reject("doc1", "admin", " bad ", "note");
        assertEquals(VerificationStatus.REJECTED, result.getStatus());
        assertEquals("bad", result.getRejectionReason());
    }

    @Test
    void rejectRequiresReasonAndHandlesNotFound() {
        assertThrows(RuntimeException.class, () -> vendorDocumentService.reject("doc1", "admin", " ", "note"));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vendorDocumentService.reject("doc1", "admin", "bad", "note"));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().id("a1").build()));
        when(vendorDocumentRepository.findById("doc1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> vendorDocumentService.reject("doc1", "admin", "bad", "note"));
    }
}
