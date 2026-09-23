package com.conectaedu.api.shared.audit.service;

import com.conectaedu.api.shared.audit.domain.AuditLog;
import com.conectaedu.api.shared.audit.repository.AuditLogRepository;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditAction;
import com.conectaedu.api.shared.enums.AuditEntityType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger CONSOLE = LoggerFactory.getLogger("AUDIT");
    private static final int MAX = 2000;

    private final AuditLogRepository AuditLogRepository;

    @Transactional
    public void logCreate(AuditEntityType type, UUID entityId, String name) {
        record(type, entityId, AuditAction.CREATE,
                "Registro de %s \"%s\" foi criado".formatted(type.getLabel(), name));
    }

    @Transactional
    public void logUpdate(AuditEntityType type, UUID entityId, String name, AuditDiff diff) {
        if (diff.isEmpty()) return;                       // nada mudou, nada a registrar
        record(type, entityId, AuditAction.UPDATE,
                "Registro de %s \"%s\" foi alterado: %s".formatted(type.getLabel(), name, diff.describe()));
    }

    @Transactional
    public void logDelete(AuditEntityType type, UUID entityId, String name) {
        record(type, entityId, AuditAction.DELETE,
                "Registro de %s \"%s\" foi excluído".formatted(type.getLabel(), name));
    }

    private void record(AuditEntityType type, UUID id, AuditAction action, String description) {
        String text = description.length() > MAX ? description.substring(0, MAX) : description;
        AuditLog entry;
        entry = AuditLogRepository.save(new AuditLog(type, id, action, text));
        CONSOLE.info("[AUDIT] {} | {} | {} | id={} | {}",
                entry.getOccurredAt(), type, action, id, text);
    }
}