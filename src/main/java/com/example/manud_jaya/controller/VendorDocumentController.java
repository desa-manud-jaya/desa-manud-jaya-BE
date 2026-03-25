package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.service.VendorDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/vendor/businesses/{businessId}/documents")
@RequiredArgsConstructor
@Tag(name = "Vendor Documents", description = "Vendor document verification upload and tracking endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class VendorDocumentController {

    private final VendorDocumentService vendorDocumentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a verification document (PDF/JPG/JPEG/PNG, max 10 MB).")
    @ApiResponse(responseCode = "200", description = "Document uploaded")
    public ResponseEntity<?> uploadDocument(
            @PathVariable String businessId,
            @Parameter(description = "Document type") @RequestParam VendorDocumentType type,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        return ResponseEntity.ok(vendorDocumentService.uploadDocument(businessId, type, file, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "Get vendor documents")
    public ResponseEntity<?> getDocuments(
            @PathVariable String businessId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vendorDocumentService.getVendorDocuments(businessId, authentication.getName()));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get document verification progress")
    public ResponseEntity<?> getProgress(
            @PathVariable String businessId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vendorDocumentService.getProgress(businessId, authentication.getName()));
    }
}
