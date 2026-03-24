package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.service.SupabaseStorageService;
import com.example.manud_jaya.service.VendorPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendorPackageControllerTest {

    @Mock
    private VendorPackageService vendorPackageService;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VendorPackageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPackageUsesAuthenticatedUsername() throws Exception {
        MockMultipartFile doc = new MockMultipartFile("requirementDocument", "doc.pdf", "application/pdf", "d".getBytes());
        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "p".getBytes());
        CreatePackageRequest request = new CreatePackageRequest();

        when(authentication.getName()).thenReturn("vendor1");
        when(supabaseStorageService.uploadDocument(any())).thenReturn("https://doc-url");
        when(supabaseStorageService.uploadImage(any())).thenReturn("https://photo-url");

        var response = controller.createPackage("business-1", request, doc, photo, authentication);

        verify(vendorPackageService).createPackage(request, "business-1", "vendor1", "https://doc-url", "https://photo-url");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getPackagesUsesAuthenticatedUsername() {
        when(authentication.getName()).thenReturn("vendor1");

        var response = controller.getPackages("business-1", authentication);

        verify(vendorPackageService).getByBusinessForVendor("business-1", "vendor1");
        assertEquals(200, response.getStatusCode().value());
    }
}
