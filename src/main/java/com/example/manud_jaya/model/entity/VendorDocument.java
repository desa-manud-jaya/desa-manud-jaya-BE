package com.example.manud_jaya.model.entity;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendor_documents")
public class VendorDocument {

    @Id
    private String id;

    private String vendorId;
    private String businessId;

    private VendorDocumentType documentType;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long fileSize;

    private VerificationStatus status;

    private String rejectionReason;
    private String reviewNote;
    private String reviewerId;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
}
