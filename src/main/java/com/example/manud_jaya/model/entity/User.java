package com.example.manud_jaya.model.entity;

import com.example.manud_jaya.model.dto.VendorProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private String role; // ADMIN, USER, VENDOR
    private String status; // ACTIVE, PENDING
    private VendorProfile vendorProfile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
