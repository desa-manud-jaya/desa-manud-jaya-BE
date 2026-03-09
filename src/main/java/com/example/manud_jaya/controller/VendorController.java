package com.example.manud_jaya.controller;

import com.example.manud_jaya.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final SupabaseStorageService storageService;

    @PostMapping("")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        String imageUrl = storageService.uploadFile(image);

        return ResponseEntity.ok(
                Map.of(
                        "imageUrl", imageUrl
                )
        );
    }
}
