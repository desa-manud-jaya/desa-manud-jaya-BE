package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookingHistoryItemResponse {
    private String id;
    private String userId;
    private String businessId;
    private String packageId;
    private String guideId;
    private String guideName;
    private LocalDate tripDate;
    private Integer quantity;
    private Double amount;
    private String status;
    private String paymentProofUrl;
    private LocalDateTime paymentUploadedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String reviewNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
