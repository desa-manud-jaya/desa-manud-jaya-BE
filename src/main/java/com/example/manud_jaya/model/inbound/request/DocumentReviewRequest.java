package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewRequest {
    private String note;
    private String reason;
}
