package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideRegisterRequest {

    private String username;
    private String email;
    private String password;

    private String fullName;
    private String phone;
    private String licenseNumber;
}
