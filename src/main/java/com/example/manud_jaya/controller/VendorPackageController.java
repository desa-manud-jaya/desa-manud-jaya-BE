package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.inbound.request.CreatePackageRequest;
import com.example.manud_jaya.service.SupabaseStorageService;
import com.example.manud_jaya.service.VendorPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/vendor/businesses/{businessId}/packages")
@RequiredArgsConstructor
public class VendorPackageController {
    private final VendorPackageService vendorPackageService;
    private final SupabaseStorageService supabaseStorageService;

    @PostMapping
    public ResponseEntity<?> createPackage(
            @PathVariable String businessId,
            @RequestPart("data") CreatePackageRequest request,
            @RequestPart("requirementDocument") MultipartFile doc,
            @RequestPart("photo") MultipartFile photo,
            @RequestHeader("vendorId") String vendorId
    ) throws IOException {

        String docUrl = supabaseStorageService.uploadDocument(doc);
        String photoUrl = supabaseStorageService.uploadImage(photo);

        vendorPackageService.createPackage(request, vendorId, businessId, docUrl, photoUrl);

        return ResponseEntity.ok("Package submitted (PENDING)");
    }

    @GetMapping
    public ResponseEntity<?> getPackages(
            @PathVariable String businessId,
            @RequestHeader String vendorId
    ) {
        return ResponseEntity.ok(vendorPackageService.getByBusiness(businessId));
    }

}
