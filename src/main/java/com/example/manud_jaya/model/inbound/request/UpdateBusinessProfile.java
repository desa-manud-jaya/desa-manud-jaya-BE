package com.example.manud_jaya.model.inbound.request;

import com.example.manud_jaya.model.dto.JenisUsaha;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessProfile {

    private String namaUsaha;
    private String namaOwner;
    private String bankAccountName;
    private String bankAccountNumber;
    private JenisUsaha jenisUsaha;
    private String email;
    private String phone;
    private String address;
    private String nik;
    private String nib;
    private String sku;
    private String siup;
}
