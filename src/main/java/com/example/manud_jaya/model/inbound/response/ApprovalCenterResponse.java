package com.example.manud_jaya.model.inbound.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCenterResponse {
    private List<ApprovalCenterItemResponse> items;
    private int page;
    private int size;
    private long total;
    private Map<String, Long> counters;
}
