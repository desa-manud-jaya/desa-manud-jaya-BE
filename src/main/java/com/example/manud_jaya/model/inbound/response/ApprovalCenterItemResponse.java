package com.example.manud_jaya.model.inbound.response;

import com.example.manud_jaya.model.dto.ApprovalQueueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCenterItemResponse {
    private ApprovalQueueType type;
    private String id;
    private String title;
    private String requestor;
    private String status;
    private LocalDateTime submittedAt;
}
