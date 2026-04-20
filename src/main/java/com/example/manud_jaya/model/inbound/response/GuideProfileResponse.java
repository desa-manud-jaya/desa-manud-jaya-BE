package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideProfileResponse {

    private String userId;
    private String username;
    private String email;
    private String role;
    private String status;

    private String fullName;
    private String phone;
    private String licenseNumber;

    private String approvalStatus;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}
