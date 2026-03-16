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

    public String uploadFile(MultipartFile file) throws IOException {

        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("File must be image");
        }

        String original = file.getOriginalFilename().replaceAll("\\s+", "_");
        String fileName = UUID.randomUUID() + "-" + original;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(file.getBytes())
        );

        // tetap pakai endpoint public Supabase
        return baseUrl +
                "/storage/v1/object/public/" +
                bucket +
                "/" +
                fileName;
    }
}
