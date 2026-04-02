package com.example.manud_jaya.service;

import com.example.manud_jaya.repository.ModerationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class ModerationAuditServiceTest {

    @Mock
    private ModerationAuditLogRepository moderationAuditLogRepository;

    @InjectMocks
    private ModerationAuditService moderationAuditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logShouldPersistAuditEntry() {
        moderationAuditService.log("PACKAGE", "APPROVE", "admin-1", "pkg-1", "ok");

        verify(moderationAuditLogRepository).save(argThat(log ->
                "PACKAGE".equals(log.getModule())
                        && "APPROVE".equals(log.getAction())
                        && "admin-1".equals(log.getActorId())
                        && "pkg-1".equals(log.getTargetId())
                        && "ok".equals(log.getNote())
                        && log.getCreatedAt() != null
        ));
    }
}
