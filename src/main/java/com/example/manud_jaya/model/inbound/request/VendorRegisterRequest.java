package com.example.manud_jaya.model.inbound.request;


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

    private String businessName;
    private String description;
    private String ktpNumber;
    private String phone;
    private String address;

}