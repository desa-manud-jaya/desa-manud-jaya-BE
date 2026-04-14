package com.example.manud_jaya.model.inbound.response;

import com.example.manud_jaya.model.entity.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDetailResponse {
    private Package packageData;
    private GuideSummaryResponse guide;
}
