package com.example.manud_jaya.model.inbound.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorPendingResponse {

    private String userId;
    private String username;
    private String email;

    private String vendorName;
    private String phone;
    private String address;

    private String ktpNumber;

}
