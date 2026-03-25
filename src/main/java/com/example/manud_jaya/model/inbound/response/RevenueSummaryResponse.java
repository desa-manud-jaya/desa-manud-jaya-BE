package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryResponse {

    private Double totalRevenue;
    private long transactionCount;
    private String startDate;
    private String endDate;
}
