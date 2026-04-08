package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDashboardAnalyticsResponse {
    private double totalRevenue;
    private long activeListings;
    private long totalBookings;
    private long pendingPackages;
    private long pendingDocuments;
    private long approvedDocuments;
}
