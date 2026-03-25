package com.example.manud_jaya.model.entity;

import com.example.manud_jaya.model.dto.ApprovalStatus;
import com.example.manud_jaya.model.dto.PackageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "packages")
public class Package {
    @Id
    private String id;
    private String name;
    private PackageCategory category;

    private BigDecimal price;
    private Integer duration;
    private Integer availability;

    private List<String> itinerary;
    private List<String> included;

    private String requirementDocumentUrl;
    private String photoUrl;

    private String approvalStatus;

    private String vendorId;
    private String businessId;

    private String rejectionReason;

    private LocalDateTime createdAt;
}
