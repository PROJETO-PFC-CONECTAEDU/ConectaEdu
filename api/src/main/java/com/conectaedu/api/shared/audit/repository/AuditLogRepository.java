package com.conectaedu.api.shared.audit.repository;

import com.conectaedu.api.shared.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
