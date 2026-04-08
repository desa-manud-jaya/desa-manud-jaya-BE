package com.example.manud_jaya.model.inbound.request;

import com.example.manud_jaya.model.dto.PackageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {
    private String name;
    private PackageCategory category;
    private BigDecimal price;
    private Integer duration;
    private Integer availability;
    private List<String> itinerary;
    private List<String> included;
    private String termsAndConditions;
    private String pricingPolicy;
    private String cancellationPolicy;
    private String moderationNote;
}
