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
public class VendorBusinessDetailResponse {
    private String userId;
    private String vendorName;
    private String phone;
    private String address;
    private String ktpNumber;
    private String approvalStatus;
    private LocalDateTime approvedAt;
}
