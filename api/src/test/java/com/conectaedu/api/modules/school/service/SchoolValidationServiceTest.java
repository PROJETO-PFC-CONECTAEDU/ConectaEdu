package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.SchoolStatus;
import com.conectaedu.api.shared.exceptions.InvalidSchoolStatusException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolValidationServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SchoolValidationService schoolValidationService;

    private School escolaPendente() {
        School escola = new School();
        escola.setId(UUID.randomUUID());
        escola.setName("Escola Estadual ABCD");
        escola.setCie("123456");
        return escola;
    }

    //a ativacao muda o status, ativa a escola e registra a mudanca na auditoria.
    @Test
    void deveAtivarEscolaPendente() {
        School escola = escolaPendente();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        SchoolResponseDTO resposta =
                schoolValidationService.validateSchool(escola.getId(), "Documentacao validada");

        assertEquals(SchoolStatus.ACTIVE.name(), resposta.status());
        assertTrue(resposta.active());
        assertEquals("Documentacao validada", escola.getValidationNotes());
        assertNotNull(escola.getUpdatedAt());
        verify(schoolRepository).save(escola);
        verify(auditService).logUpdate(
                eq(AuditEntityType.SCHOOL), eq(escola.getId()),
                eq("Escola Estadual ABCD"), capturado.capture());
        AuditDiff alteracoes = capturado.getValue();
        assertTrue(alteracoes.describe().contains("status"));
        assertTrue(alteracoes.describe().contains("active"));
    }

    @Test
    void deveInativarEscolaPendente() {
        School escola = escolaPendente();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));

        SchoolResponseDTO resposta = schoolValidationService.deactivateSchool(escola.getId());

        assertEquals(SchoolStatus.INACTIVE.name(), resposta.status());
        assertFalse(resposta.active());
        assertNotNull(escola.getValidatedAt());
        verify(schoolRepository).save(escola);
        verify(auditService).logUpdate(
                eq(AuditEntityType.SCHOOL), eq(escola.getId()), eq("Escola Estadual ABCD"), any(AuditDiff.class));
    }

    //escola que saiu de PENDING_ACTIVATION nao volta para validacao.
    @Test
    void deveLancarExcecaoQuandoEscolaJaFoiAvaliada() {
        School escola = escolaPendente();
        escola.setStatus(SchoolStatus.ACTIVE);
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));

        InvalidSchoolStatusException erro = assertThrows(InvalidSchoolStatusException.class,
                () -> schoolValidationService.validateSchool(escola.getId(), "Reavaliando"));

        assertEquals("Escola já ativa!", erro.getMessage());
        verify(schoolRepository, never()).save(any(School.class));
        verify(auditService, never()).logUpdate(any(), any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoEscolaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(schoolRepository.findById(id)).thenReturn(Optional.empty());

        SchoolNotFoundException erro = assertThrows(SchoolNotFoundException.class,
                () -> schoolValidationService.validateSchool(id, "Parecer qualquer"));

        assertEquals("Escola não encontrada!", erro.getMessage());
        verify(schoolRepository, never()).save(any(School.class));
        verify(auditService, never()).logUpdate(any(), any(), any(), any());
    }

    @Test
    void deveAtivarSemParecerPorqueAJustificativaNaoEObrigatoria() {
        School escola = escolaPendente();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));

        SchoolResponseDTO resposta = schoolValidationService.validateSchool(escola.getId(), null);

        assertEquals(SchoolStatus.ACTIVE.name(), resposta.status());
        assertTrue(resposta.active());
        assertNull(escola.getValidationNotes());
        verify(schoolRepository).save(escola);
    }
}
