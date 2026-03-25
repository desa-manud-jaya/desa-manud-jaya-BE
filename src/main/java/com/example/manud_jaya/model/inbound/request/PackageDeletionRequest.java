package com.example.manud_jaya.model.inbound.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDeletionRequest {
    private String reason;
}
