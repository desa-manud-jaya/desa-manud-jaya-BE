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
public class VendorProfile {


    private String vendorName;
    private String ownerName;
    private JenisUsaha jenisUsaha;
    private String bankAccountNumber;
    private String bankAccountName;
    private String sustainabilityCertification;
    private String nik;
    private String nib;
    private String sku;
    private String siup;
    private String description;
    private String phone;
    private String ktpNumber;
    private String address;
    private String approvalStatus; // PENDING, APPROVED, PLEASE_FILL_PROFILE
    private LocalDateTime approvedAt;

}
