package com.example.manud_jaya.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideProfile {

    private String fullName;
    private String phone;
    private String licenseNumber;

    private String approvalStatus;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}
