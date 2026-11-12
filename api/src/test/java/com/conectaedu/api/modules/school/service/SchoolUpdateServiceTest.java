package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolUpdateRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolUpdateServiceTest {

    private static final String CIE = "123456";
    private static final String NOME_ORIGINAL = "Escola Estadual ABCD";
    private static final String DIRETOR_ORIGINAL = "Maria Sousa";
    private static final String ENDERECO_ORIGINAL = "R. teste, 100";
    private static final double LATITUDE_ORIGINAL = -23.5505;
    private static final double LONGITUDE_ORIGINAL = -46.6333;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SchoolUpdateService schoolUpdateService;

    private School escolaCadastrada() {
        School escola = new School();
        escola.setId(UUID.randomUUID());
        escola.setName(NOME_ORIGINAL);
        escola.setCie(CIE);
        escola.setDirector(DIRETOR_ORIGINAL);
        escola.setAddress(ENDERECO_ORIGINAL);
        escola.setLatitude(LATITUDE_ORIGINAL);
        escola.setLongitude(LONGITUDE_ORIGINAL);
        return escola;
    }

    //a alteracao grava os novos dados e registra o que mudou.
    @Test
    void deveAtualizarCadastroERegistrarAsAlteracoesNaAuditoria() {
        School escola = escolaCadastrada();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        SchoolResponseDTO resposta = schoolUpdateService.updateSchool(
                escola.getId(),
                new SchoolUpdateRequestDTO("Escola Estadual Abcde", "Joao Paulo", "Av. nova, 500", -22.9068, -43.1729));

        assertEquals("Escola Estadual Abcde", resposta.name());
        assertEquals("Joao Paulo", resposta.director());
        assertEquals("Av. nova, 500", resposta.address());
        assertEquals(-22.9068, resposta.latitude());
        assertEquals(-43.1729, resposta.longitude());
        assertEquals(CIE, resposta.cie());
        assertNotNull(escola.getUpdatedAt());
        verify(schoolRepository).save(escola);
        verify(auditService).logUpdate(
                eq(AuditEntityType.SCHOOL), eq(escola.getId()),
                eq("Escola Estadual Abcde"), capturado.capture());
        AuditDiff alteracoes = capturado.getValue();
        assertFalse(alteracoes.isEmpty());
        assertTrue(alteracoes.describe().contains("name"));
        assertTrue(alteracoes.describe().contains("director"));
        assertTrue(alteracoes.describe().contains("address"));
    }

    // nao e possivel altera o que nao existe.
    @Test
    void deveLancarExcecaoAoAtualizarEscolaInexistente() {
        UUID id = UUID.randomUUID();
        when(schoolRepository.findById(id)).thenReturn(Optional.empty());

        SchoolNotFoundException erro = assertThrows(SchoolNotFoundException.class,
                () -> schoolUpdateService.updateSchool(
                        id, new SchoolUpdateRequestDTO("Escola XYZ", "Joao P", "Av. nova, 500", null, null)));

        assertEquals("Escola não encontrada!", erro.getMessage());
        verify(schoolRepository, never()).save(any(School.class));
        verifyNoInteractions(auditService);
    }

    @Test
    void naoDeveRegistrarAlteracaoQuandoOsDadosEnviadosSaoIguaisAosAtuais() {
        School escola = escolaCadastrada();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        schoolUpdateService.updateSchool(
                escola.getId(),
                new SchoolUpdateRequestDTO(NOME_ORIGINAL, DIRETOR_ORIGINAL, ENDERECO_ORIGINAL,
                        LATITUDE_ORIGINAL, LONGITUDE_ORIGINAL));

        verify(auditService).logUpdate(any(), any(), any(), capturado.capture());
        AuditDiff alteracoes = capturado.getValue();
        assertTrue(alteracoes.isEmpty());
        assertEquals("", alteracoes.describe());
        assertEquals(NOME_ORIGINAL, escola.getName());
    }

    @Test
    void deveManterDadosAtuaisQuandoCamposNaoSaoEnviados() {
        School escola = escolaCadastrada();
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        SchoolResponseDTO resposta = schoolUpdateService.updateSchool(
                escola.getId(), new SchoolUpdateRequestDTO(null, null, null, null, null));

        assertEquals(NOME_ORIGINAL, resposta.name());
        assertEquals(DIRETOR_ORIGINAL, resposta.director());
        assertEquals(ENDERECO_ORIGINAL, resposta.address());
        assertEquals(LATITUDE_ORIGINAL, resposta.latitude());
        assertEquals(LONGITUDE_ORIGINAL, resposta.longitude());
        verify(schoolRepository).save(escola);
        verify(auditService).logUpdate(any(), any(), any(), capturado.capture());
        assertTrue(capturado.getValue().isEmpty());
    }
}
