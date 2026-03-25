package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.entity.VendorDocument;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadDocumentSuccess() throws Exception {
        User vendor = User.builder().id("v-1").username("vendor1").build();
        Business business = Business.builder().id("b-1").vendorId("v-1").build();

        MockMultipartFile file = new MockMultipartFile("file", "ktp.pdf", "application/pdf", "abc".getBytes());

        when(userRepository.findByUsername("vendor1")).thenReturn(Optional.of(vendor));
        when(businessRepository.findById("b-1")).thenReturn(Optional.of(business));
        when(supabaseStorageService.uploadDocument(any())).thenReturn("https://doc-url");
        when(vendorDocumentRepository.save(any(VendorDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        VendorDocument saved = vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1");

        assertEquals(VerificationStatus.PENDING, saved.getStatus());
        assertEquals("https://doc-url", saved.getFileUrl());
    }

    @Test
    void uploadDocumentRejectsLargeFile() {
        byte[] bytes = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "too-big.pdf", "application/pdf", bytes);

        assertThrows(RuntimeException.class, () ->
                vendorDocumentService.uploadDocument("b-1", VendorDocumentType.KTP, file, "vendor1")
        );
    }

    @Test
    void rejectRequiresReason() {
        assertThrows(RuntimeException.class, () ->
                vendorDocumentService.reject("doc-1", "admin", " ", "note")
        );
    }
}
