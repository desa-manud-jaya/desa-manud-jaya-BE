package com.example.manud_jaya.repository;

import com.example.manud_jaya.model.entity.ModerationAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationAuditLogRepository extends MongoRepository<ModerationAuditLog, String> {
}
