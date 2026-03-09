package com.example.manud_jaya.service;

import com.example.manud_jaya.model.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;

    public String uploadFile(MultipartFile file) throws IOException {

        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("File must be image");
        }

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        String uploadUrl =
                supabaseConfig.getUrl()
                        + "/storage/v1/object/"
                        + supabaseConfig.getBucket()
                        + "/"
                        + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("apikey", supabaseConfig.getApiKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getApiKey());

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        return supabaseConfig.getUrl()
                + "/storage/v1/object/public/"
                + supabaseConfig.getBucket()
                + "/"
                + fileName;
    }
}
