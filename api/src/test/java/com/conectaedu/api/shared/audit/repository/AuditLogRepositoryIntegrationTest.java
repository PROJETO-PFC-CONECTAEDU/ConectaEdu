package com.conectaedu.api.shared.audit.repository;

import com.conectaedu.api.shared.audit.domain.AuditLog;
import com.conectaedu.api.shared.enums.AuditAction;
import com.conectaedu.api.shared.enums.AuditEntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class AuditLogRepositoryIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void deveSalvarERecuperarRegistroDeAuditoria() {
        UUID universidadeId = UUID.randomUUID();
        AuditLog salvo = auditLogRepository.saveAndFlush(new AuditLog(
                AuditEntityType.UNIVERSITY, universidadeId, AuditAction.CREATE,
                "Registro de Universidade \"Universidade ABCD\" foi criado", "admin@conectaedu.com"));

        Optional<AuditLog> encontrado = auditLogRepository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertNotNull(encontrado.get().getId());
        assertEquals(AuditEntityType.UNIVERSITY, encontrado.get().getEntityType());
        assertEquals(universidadeId, encontrado.get().getEntityId());
        assertEquals(AuditAction.CREATE, encontrado.get().getAction());
        assertEquals("Registro de Universidade \"Universidade ABCD\" foi criado",
                encontrado.get().getDescription());
        assertEquals("admin@conectaedu.com", encontrado.get().getPerformedBy());
        assertEquals(LocalDate.now(), encontrado.get().getOccurredAt());
    }

    @Test
    void deveSalvarRegistroSemEntidadeEComDescricaoNoTamanhoMaximo() {
        String descricao = "A".repeat(2000);

        AuditLog salvo = auditLogRepository.saveAndFlush(new AuditLog(
                AuditEntityType.LESSON, null, AuditAction.DELETE, descricao, "SYSTEM"));

        assertNotNull(salvo.getId());
        assertNull(salvo.getEntityId());
        assertEquals(2000, salvo.getDescription().length());
        assertEquals(1, auditLogRepository.count());
    }
}
