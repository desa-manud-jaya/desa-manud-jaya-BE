package com.example.manud_jaya.service;

import com.example.manud_jaya.model.entity.ModerationAuditLog;
import com.example.manud_jaya.repository.ModerationAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModerationAuditService {

    private final ModerationAuditLogRepository moderationAuditLogRepository;

    public void log(String module, String action, String actorId, String targetId, String note) {
        ModerationAuditLog log = ModerationAuditLog.builder()
                .module(module)
                .action(action)
                .actorId(actorId)
                .targetId(targetId)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();

        moderationAuditLogRepository.save(log);
    }
}
