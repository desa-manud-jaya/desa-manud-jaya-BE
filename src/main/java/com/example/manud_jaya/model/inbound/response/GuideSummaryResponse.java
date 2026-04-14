package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideSummaryResponse {
    private String id;
    private String fullName;
    private String phone;
    private String status;
}
