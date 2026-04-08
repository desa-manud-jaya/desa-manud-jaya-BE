package com.example.manud_jaya.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "moderation_audit_logs")
public class ModerationAuditLog {

    @Id
    private String id;

    private String module;
    private String action;
    private String actorId;
    private String targetId;
    private String note;
    private LocalDateTime createdAt;
}
