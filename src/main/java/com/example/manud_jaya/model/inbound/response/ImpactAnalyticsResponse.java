package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactAnalyticsResponse {
    private long totalDestinations;
    private long totalReviews;
    private double averageRating;
    private long totalConfirmedBookings;
}
