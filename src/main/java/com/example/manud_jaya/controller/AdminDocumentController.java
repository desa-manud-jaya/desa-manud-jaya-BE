package com.example.manud_jaya.controller;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.inbound.request.DocumentReviewRequest;
import com.example.manud_jaya.service.VendorDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
@Tag(name = "Admin Documents", description = "Admin document verification moderation endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class AdminDocumentController {

    private final VendorDocumentService vendorDocumentService;

    @GetMapping("/pending")
    @Operation(summary = "Get pending documents")
    public ResponseEntity<?> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(vendorDocumentService.getPendingDocuments(page, size));
    }

    @GetMapping
    @Operation(summary = "Get documents queue", description = "Filterable documents queue by status/type with pagination.")
    public ResponseEntity<?> getDocuments(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) VendorDocumentType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(vendorDocumentService.getDocuments(status, type, page, size));
    }

    @PutMapping("/{documentId}/approve")
    @Operation(summary = "Approve document")
    @ApiResponse(responseCode = "200", description = "Document approved")
    public ResponseEntity<?> approve(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @RequestBody(required = false) DocumentReviewRequest request,
            Authentication authentication
    ) {
        String note = request == null ? null : request.getNote();
        return ResponseEntity.ok(vendorDocumentService.approve(documentId, authentication.getName(), note));
    }

    @PutMapping("/{documentId}/reject")
    @Operation(summary = "Reject document")
    @ApiResponse(responseCode = "200", description = "Document rejected")
    public ResponseEntity<?> reject(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @RequestBody DocumentReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vendorDocumentService.reject(
                documentId,
                authentication.getName(),
                request.getReason(),
                request.getNote()
        ));
    }
}
