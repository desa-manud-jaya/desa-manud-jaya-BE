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
public class VendorRegisterRequest {

    private String username;
    private String email;
    private String password;
    private JenisUsaha jenisUsaha;
    private String namaUsaha;
    private String namaOwner;
    private String description;
    private String ktpNumber;
    private String phone;
    private String address;

}