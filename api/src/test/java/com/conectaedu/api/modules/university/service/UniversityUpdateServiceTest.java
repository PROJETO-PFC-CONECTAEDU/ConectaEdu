package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.request.UniversityUpdateRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//Regra, a alteracao do cadastro so registra auditoria quando algum dado muda

@ExtendWith(MockitoExtension.class)
class UniversityUpdateServiceTest {

    private static final String NOME_ORIGINAL = "Universidade ABCD";
    private static final String COORDENADOR_ORIGINAL = "Joao P";
    private static final String ENDERECO_ORIGINAL = "R. teste, 200";

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UniversityUpdateService universityUpdateService;

    private University universidadeCadastrada() {
        University universidade = new University();
        universidade.setId(UUID.randomUUID());
        universidade.setName(NOME_ORIGINAL);
        universidade.setCnpj("12345671000112");
        universidade.setCoordinator(COORDENADOR_ORIGINAL);
        universidade.setAddress(ENDERECO_ORIGINAL);
        return universidade;
    }

    //A alteracao grava os novos dados e registra o que mudou.
    @Test
    void deveAtualizarCadastroERegistrarAsAlteracoesNaAuditoria() {
        // preparacao
        University universidade = universidadeCadastrada();
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        // execucao
        UniversityResponseDTO resposta = universityUpdateService.updateUniversity(
                universidade.getId(),
                new UniversityUpdateRequestDTO("Universidade ABCDE", "Maria S", "Av. nova, 500"));

        // verificacao
        assertEquals("Universidade ABCDE", resposta.name());
        assertEquals("Maria S", resposta.coordinator());
        assertEquals("Av. nova, 500", resposta.address());
        assertEquals("12345671000112", resposta.cnpj());
        verify(universityRepository).save(universidade);
        verify(auditService).logUpdate(
                eq(AuditEntityType.UNIVERSITY), eq(universidade.getId()),
                eq("Universidade ABCDE"), capturado.capture());
        AuditDiff alteracoes = capturado.getValue();
        assertFalse(alteracoes.isEmpty());
        assertTrue(alteracoes.describe().contains("name"));
        assertTrue(alteracoes.describe().contains("coordinator"));
        assertTrue(alteracoes.describe().contains("address"));
    }

    // nao se altera o que nao existe.
    @Test
    void deveLancarExcecaoAoAtualizarUniversidadeInexistente() {
        // preparacao
        UUID id = UUID.randomUUID();
        when(universityRepository.findById(id)).thenReturn(Optional.empty());

        // execucao
        UniversityNotFoundException erro = assertThrows(UniversityNotFoundException.class,
                () -> universityUpdateService.updateUniversity(
                        id, new UniversityUpdateRequestDTO("Universidade XYZ", "Maria S", "Av. nova, 500")));

        // verificacao
        assertEquals("Universidade não encontrada!", erro.getMessage());
        verify(universityRepository, never()).save(any(University.class));
        verifyNoInteractions(auditService);
    }

    //O cadastro continua igual e recebe um conjunto vazio de mudancas.
    @Test
    void naoDeveRegistrarAlteracaoQuandoOsDadosEnviadosSaoIguaisAosAtuais() {
        // preparacao
        University universidade = universidadeCadastrada();
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));
        ArgumentCaptor<AuditDiff> capturado = ArgumentCaptor.forClass(AuditDiff.class);

        // execucao
        universityUpdateService.updateUniversity(
                universidade.getId(),
                new UniversityUpdateRequestDTO(NOME_ORIGINAL, COORDENADOR_ORIGINAL, ENDERECO_ORIGINAL));

        // verificacao
        verify(auditService).logUpdate(any(), any(), any(), capturado.capture());
        AuditDiff alteracoes = capturado.getValue();
        assertTrue(alteracoes.isEmpty());
        assertEquals("", alteracoes.describe());
        assertEquals(NOME_ORIGINAL, universidade.getName());
    }
}

