package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.service.PublicBusinessService;
import com.example.manud_jaya.service.SupabaseStorageService;
import com.example.manud_jaya.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final SupabaseStorageService storageService;
    private final PublicBusinessService publicBusinessService;

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

    @GetMapping("/{vendorId}/business")
    public ResponseEntity<?> getBusiness(
            @PathVariable String vendorId
    ) {
        Business business = publicBusinessService.getBusinessByVendor(vendorId);
        return ResponseEntity.ok(business);
    }
}
