package com.example.manud_jaya.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final S3Client s3Client;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.url}")
    private String baseUrl;

    public String uploadImage(MultipartFile file) throws IOException {
        validateImage(file);
        return upload(file, "images/");
    }

    public String uploadDocument(MultipartFile file) throws IOException {
        validateDocument(file);
        return upload(file, "documents/");
    }

    private String upload(MultipartFile file, String folder) throws IOException {

        String original = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("\\s+", "_")
                : "file";

        String fileName = folder + UUID.randomUUID() + "-" + original;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return buildPublicUrl(fileName);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
    }

    private void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10 MB");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        boolean isPdf = "application/pdf".equals(contentType)
                || ("application/octet-stream".equals(contentType) && filename.endsWith(".pdf"));

        boolean isSupportedImage = "image/jpeg".equals(contentType)
                || "image/jpg".equals(contentType)
                || "image/png".equals(contentType);

        if (!isPdf && !isSupportedImage) {
            throw new RuntimeException("Invalid document type. Supported: PDF/JPG/JPEG/PNG");
        }
    }

    private String buildPublicUrl(String fileName) {
        return String.format("%s/storage/v1/object/public/%s/%s",
                baseUrl, bucket, fileName);
    }
}
