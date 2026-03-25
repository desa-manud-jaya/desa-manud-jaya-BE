package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDocumentProgressResponse {
    private long total;
    private long pending;
    private long approved;
    private long rejected;
    private double completionRate;
}
