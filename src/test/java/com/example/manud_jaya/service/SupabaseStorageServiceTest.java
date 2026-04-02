package com.example.manud_jaya.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SupabaseStorageServiceTest {

    private S3Client s3Client;
    private SupabaseStorageService supabaseStorageService;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        supabaseStorageService = new SupabaseStorageService(s3Client);
        ReflectionTestUtils.setField(supabaseStorageService, "bucket", "bucket-test");
        ReflectionTestUtils.setField(supabaseStorageService, "baseUrl", "https://example.supabase.co");
    }

    @Test
    void uploadImageShouldUploadAndReturnPublicUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "abc".getBytes());
        String url = supabaseStorageService.uploadImage(file);

        assertTrue(url.contains("https://example.supabase.co/storage/v1/object/public/bucket-test/images/"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertTrue(captor.getValue().key().startsWith("images/"));
    }

    @Test
    void uploadImageInvalidTypeShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "abc".getBytes());
        assertThrows(RuntimeException.class, () -> supabaseStorageService.uploadImage(file));
    }

    @Test
    void uploadImageEmptyShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);
        assertThrows(RuntimeException.class, () -> supabaseStorageService.uploadImage(file));
    }

    @Test
    void uploadDocumentShouldSupportPdfJpgPngAndOctetPdf() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "a.pdf", "application/pdf", "abc".getBytes());
        MockMultipartFile jpg = new MockMultipartFile("file", "a.jpg", "image/jpeg", "abc".getBytes());
        MockMultipartFile png = new MockMultipartFile("file", "a.png", "image/png", "abc".getBytes());
        MockMultipartFile octetPdf = new MockMultipartFile("file", "a.pdf", "application/octet-stream", "abc".getBytes());

        assertTrue(supabaseStorageService.uploadDocument(pdf).contains("/documents/"));
        assertTrue(supabaseStorageService.uploadDocument(jpg).contains("/documents/"));
        assertTrue(supabaseStorageService.uploadDocument(png).contains("/documents/"));
        assertTrue(supabaseStorageService.uploadDocument(octetPdf).contains("/documents/"));
    }

    @Test
    void uploadDocumentInvalidTypeShouldThrow() {
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", "abc".getBytes());
        assertThrows(RuntimeException.class, () -> supabaseStorageService.uploadDocument(file));
    }

    @Test
    void uploadDocumentTooLargeShouldThrow() {
        byte[] payload = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", payload);
        assertThrows(RuntimeException.class, () -> supabaseStorageService.uploadDocument(file));
    }
}
