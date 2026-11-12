package com.conectaedu.api.shared.audit.service;

import com.conectaedu.api.shared.audit.domain.AuditLog;
import com.conectaedu.api.shared.audit.repository.AuditLogRepository;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditAction;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
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

    @Transactional
    public void logPasswordResetRequest(UUID userId, String email) {
        record(AuditEntityType.USER, userId, AuditAction.PASSWORD_RESET_REQUEST,
                "Solicitação de recuperação de senha para o e-mail: %s".formatted(email));
    }

    @Transactional
    public void logPasswordResetSuccess(UUID userId, String email) {
        record(AuditEntityType.USER, userId, AuditAction.PASSWORD_RESET_SUCCESS,
                "Senha do usuário %s resetada com sucesso".formatted(email));
    }

    @Transactional
    public void logPasswordResetFail(UUID userId, String email, String reason) {
        record(AuditEntityType.USER, userId, AuditAction.PASSWORD_RESET_FAIL,
                "Falha no reset de senha para %s: %s".formatted(email, reason));
    }

    @Transactional
    public void logEmailConfirmationSuccess(UUID userId, String email) {
        record(AuditEntityType.USER, userId, AuditAction.EMAIL_CONFIRMATION_SUCCESS,
                "E-mail do usuário %s confirmado com sucesso".formatted(email));
    }

    @Transactional
    public void logEmailConfirmationFail(String token, String reason) {
        record(AuditEntityType.USER, null, AuditAction.EMAIL_CONFIRMATION_FAIL,
                "Falha na confirmação de e-mail com token %s: %s".formatted(token, reason));
    }

    private void record(AuditEntityType type, UUID id, AuditAction action, String description) {
        String text = description.length() > MAX ? description.substring(0, MAX) : description;
        String performedBy = SecurityContextHolder.getAuthenticatedUserEmail();
        if (performedBy == null) {
            performedBy = "SYSTEM";
        }
        AuditLog entry;
        entry = AuditLogRepository.save(new AuditLog(type, id, action, text, performedBy));
        CONSOLE.info("[AUDIT] {} | {} | {} | {} | id={} | {}",
                entry.getOccurredAt(), performedBy, type, action, id, text);
    }
}