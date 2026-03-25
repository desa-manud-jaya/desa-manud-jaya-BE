package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardAnalyticsResponse {
    private long pendingPartnerApplications;
    private long pendingPackageApprovals;
    private long pendingDeletionRequests;
    private long pendingDocumentVerifications;
    private long activePartners;
    private long activePackages;
    private double platformRevenue;
}
