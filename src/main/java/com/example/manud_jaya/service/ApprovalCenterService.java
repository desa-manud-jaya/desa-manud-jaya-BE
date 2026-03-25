package com.example.manud_jaya.service;

import com.example.manud_jaya.model.dto.ApprovalQueueType;
import com.example.manud_jaya.model.dto.DeletionRequestStatus;
import com.example.manud_jaya.model.dto.VerificationStatus;
import com.example.manud_jaya.model.entity.Package;
import com.example.manud_jaya.model.entity.User;
import com.example.manud_jaya.model.entity.VendorDocument;
import com.example.manud_jaya.model.inbound.response.ApprovalCenterItemResponse;
import com.example.manud_jaya.model.inbound.response.ApprovalCenterResponse;
import com.example.manud_jaya.repository.PackageRepository;
import com.example.manud_jaya.repository.UserRepository;
import com.example.manud_jaya.repository.VendorDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApprovalCenterService {

    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final VendorDocumentRepository vendorDocumentRepository;

    public ApprovalCenterResponse getQueue(String type,
                                           String status,
                                           String q,
                                           int page,
                                           int size,
                                           String sortDir) {
        List<ApprovalCenterItemResponse> rows = new ArrayList<>();

        boolean includePartner = type == null || "PARTNER_REQUEST".equalsIgnoreCase(type);
        boolean includePackage = type == null || "PACKAGE_REQUEST".equalsIgnoreCase(type);
        boolean includeDeletion = type == null || "PACKAGE_DELETION_REQUEST".equalsIgnoreCase(type);
        boolean includeDocument = type == null || "DOCUMENT_VERIFICATION_REQUEST".equalsIgnoreCase(type);

        if (includePartner) {
            for (User user : userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", "PENDING")) {
                rows.add(ApprovalCenterItemResponse.builder()
                        .type(ApprovalQueueType.PARTNER_REQUEST)
                        .id(user.getId())
                        .title(user.getVendorProfile() != null ? user.getVendorProfile().getVendorName() : user.getUsername())
                        .requestor(user.getUsername())
                        .status("PENDING")
                        .submittedAt(user.getCreatedAt())
                        .build());
            }
        }

        if (includePackage) {
            for (Package pkg : packageRepository.findByApprovalStatus("PENDING")) {
                rows.add(ApprovalCenterItemResponse.builder()
                        .type(ApprovalQueueType.PACKAGE_REQUEST)
                        .id(pkg.getId())
                        .title(pkg.getName())
                        .requestor(pkg.getVendorId())
                        .status(pkg.getApprovalStatus())
                        .submittedAt(pkg.getCreatedAt())
                        .build());
            }
        }

        if (includeDeletion) {
            for (Package pkg : packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING)) {
                rows.add(ApprovalCenterItemResponse.builder()
                        .type(ApprovalQueueType.PACKAGE_DELETION_REQUEST)
                        .id(pkg.getId())
                        .title(pkg.getName())
                        .requestor(pkg.getVendorId())
                        .status(pkg.getDeletionRequestStatus().name())
                        .submittedAt(pkg.getDeletionRequestedAt())
                        .build());
            }
        }

        if (includeDocument) {
            for (VendorDocument doc : vendorDocumentRepository.findByStatus(VerificationStatus.PENDING)) {
                rows.add(ApprovalCenterItemResponse.builder()
                        .type(ApprovalQueueType.DOCUMENT_VERIFICATION_REQUEST)
                        .id(doc.getId())
                        .title(doc.getDocumentType() == null ? "DOCUMENT" : doc.getDocumentType().name())
                        .requestor(doc.getVendorId())
                        .status(doc.getStatus().name())
                        .submittedAt(doc.getSubmittedAt())
                        .build());
            }
        }

        if (status != null && !status.isBlank()) {
            rows = rows.stream()
                    .filter(it -> status.equalsIgnoreCase(it.getStatus()))
                    .toList();
        }

        if (q != null && !q.isBlank()) {
            String query = q.toLowerCase(Locale.ROOT);
            rows = rows.stream()
                    .filter(it -> {
                        String title = it.getTitle() == null ? "" : it.getTitle().toLowerCase(Locale.ROOT);
                        String requestor = it.getRequestor() == null ? "" : it.getRequestor().toLowerCase(Locale.ROOT);
                        return title.contains(query) || requestor.contains(query);
                    })
                    .toList();
        }

        Comparator<ApprovalCenterItemResponse> comparator = Comparator
                .comparing(ApprovalCenterItemResponse::getSubmittedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));

        if (!"asc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        rows = rows.stream().sorted(comparator).toList();

        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());

        Map<String, Long> counters = new HashMap<>();
        counters.put("partner", (long) userRepository.findByRoleAndVendorProfileApprovalStatus("VENDOR", "PENDING").size());
        counters.put("package", packageRepository.countByApprovalStatus("PENDING"));
        counters.put("deletion", (long) packageRepository.findByDeletionRequestStatus(DeletionRequestStatus.PENDING).size());
        counters.put("document", vendorDocumentRepository.countByStatus(VerificationStatus.PENDING));

        return ApprovalCenterResponse.builder()
                .items(rows.subList(from, to))
                .page(page)
                .size(size)
                .total(rows.size())
                .counters(counters)
                .build();
    }
}
