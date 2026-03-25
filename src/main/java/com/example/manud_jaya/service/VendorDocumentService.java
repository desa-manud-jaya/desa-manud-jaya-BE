package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.dto.VendorDocumentType;
import com.example.manud_jaya.model.entity.Business;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.entity.VendorDocument;
import com.example.manud_jaya.model.inbound.response.PagedResponse;
import com.example.manud_jaya.model.inbound.response.VendorDocumentProgressResponse;
import com.example.manud_jaya.repository.BusinessRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorDocumentService {

    private static final long MAX_SIZE = 10 * 1024 * 1024;

    private final VendorDocumentRepository vendorDocumentRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final ModerationAuditService moderationAuditService;

    public VendorDocument uploadDocument(String businessId,
                                         VendorDocumentType type,
                                         MultipartFile file,
                                         String username) throws IOException {

        validateDocumentFile(file);

        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        String fileUrl = supabaseStorageService.uploadDocument(file);

        VendorDocument document = VendorDocument.builder()
                .vendorId(vendor.getId())
                .businessId(businessId)
                .documentType(type)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .status(VerificationStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        VendorDocument saved = vendorDocumentRepository.save(document);
        moderationAuditService.log("DOCUMENT", "SUBMIT", vendor.getId(), saved.getId(), "Vendor submitted document");
        return saved;
    }

    public List<VendorDocument> getVendorDocuments(String businessId, String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return vendorDocumentRepository.findByVendorIdAndBusinessId(vendor.getId(), businessId)
                .stream()
                .sorted(Comparator.comparing(VendorDocument::getSubmittedAt).reversed())
                .toList();
    }

    public VendorDocumentProgressResponse getProgress(String businessId, String username) {
        User vendor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!business.getVendorId().equals(vendor.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        long total = vendorDocumentRepository.findByVendorIdAndBusinessId(vendor.getId(), businessId).size();
        long pending = vendorDocumentRepository.countByBusinessIdAndStatus(businessId, VerificationStatus.PENDING);
        long approved = vendorDocumentRepository.countByBusinessIdAndStatus(businessId, VerificationStatus.APPROVED);
        long rejected = vendorDocumentRepository.countByBusinessIdAndStatus(businessId, VerificationStatus.REJECTED);

        double completionRate = total == 0 ? 0 : (approved * 100.0 / total);

        return VendorDocumentProgressResponse.builder()
                .total(total)
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .completionRate(completionRate)
                .build();
    }

    public PagedResponse<VendorDocument> getPendingDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<VendorDocument> items = vendorDocumentRepository.findByStatus(VerificationStatus.PENDING, pageable);
        long total = vendorDocumentRepository.countByStatus(VerificationStatus.PENDING);
        return PagedResponse.<VendorDocument>builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .build();
    }

    public PagedResponse<VendorDocument> getDocuments(VerificationStatus status,
                                                      VendorDocumentType type,
                                                      int page,
                                                      int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<VendorDocument> items;
        long total;

        if (status == null) {
            items = vendorDocumentRepository.findAll(pageable).getContent();
            total = vendorDocumentRepository.count();
        } else if (type != null) {
            items = vendorDocumentRepository.findByStatusAndDocumentType(status, type, pageable);
            total = items.size();
        } else {
            items = vendorDocumentRepository.findByStatus(status, pageable);
            total = vendorDocumentRepository.countByStatus(status);
        }

        return PagedResponse.<VendorDocument>builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .build();
    }

    public VendorDocument approve(String documentId, String adminUsername, String note) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        VendorDocument doc = vendorDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setStatus(VerificationStatus.APPROVED);
        doc.setRejectionReason(null);
        doc.setReviewNote(note);
        doc.setReviewerId(admin.getId());
        doc.setReviewedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        VendorDocument saved = vendorDocumentRepository.save(doc);
        moderationAuditService.log("DOCUMENT", "APPROVE", admin.getId(), saved.getId(), note);
        return saved;
    }

    public VendorDocument reject(String documentId, String adminUsername, String reason, String note) {
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Rejection reason is required");
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        VendorDocument doc = vendorDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.setStatus(VerificationStatus.REJECTED);
        doc.setRejectionReason(reason.trim());
        doc.setReviewNote(note);
        doc.setReviewerId(admin.getId());
        doc.setReviewedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        VendorDocument saved = vendorDocumentRepository.save(doc);
        moderationAuditService.log("DOCUMENT", "REJECT", admin.getId(), saved.getId(), reason);
        return saved;
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File size exceeds 10 MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Invalid file type");
        }

        boolean allowed = contentType.equals("application/pdf")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/png");

        if (!allowed) {
            throw new RuntimeException("Supported types: PDF/JPG/JPEG/PNG");
        }
    }
}
