package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.conectaedu.api.shared.exceptions.InvalidUniversityStatusException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//Regra, so universidade PENDING entra em validacao.
//Aprovacao ativa a instituicao, recusa a mantem inativa e exige justificativa.

@ExtendWith(MockitoExtension.class)
class UniversityValidationServiceTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UniversityValidationService universityValidationService;

    private University universidadePendente() {
        University universidade = new University();
        universidade.setId(UUID.randomUUID());
        universidade.setName("Universidade ABCD");
        universidade.setCnpj("12345671000112");
        return universidade;
    }

    //A aprovacao muda o status e libera a universidade para receber estudantes.
    @Test
    void deveAprovarEAtivarUniversidadePendente() {
        // preparacao
        University universidade = universidadePendente();
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));

        // execucao
        UniversityResponseDTO resposta =
                universityValidationService.validateUniversity(universidade.getId(), "Documentacao validada");

        // verificacao
        assertEquals(UniversityStatus.APPROVED.name(), resposta.status());
        assertTrue(resposta.active());
        assertTrue(resposta.canReceiveStudents());
        assertNotNull(resposta.validatedAt());
        assertEquals("Documentacao validada", resposta.validationNotes());
        verify(universityRepository).save(universidade);
    }

    //Fluxo da recusa, o status muda, mas a universidade continua inativa.
    @Test
    void deveRecusarUniversidadeMantendoInativa() {
        // preparacao
        University universidade = universidadePendente();
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));

        // execucao
        UniversityResponseDTO resposta =
                universityValidationService.rejectUniversity(universidade.getId(), "CNPJ divergente");

        // verificacao
        assertEquals(UniversityStatus.REJECTED.name(), resposta.status());
        assertFalse(resposta.active());
        assertFalse(resposta.canReceiveStudents());
        verify(universityRepository).save(universidade);
    }

    //APPROVED e REJECTED sao situacoes finais, nao voltam para validacao.
    @Test
    void deveLancarExcecaoQuandoUniversidadeJaFoiAvaliada() {
        // preparacao
        University universidade = universidadePendente();
        universidade.setStatus(UniversityStatus.APPROVED);
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));

        // execucao
        InvalidUniversityStatusException erro = assertThrows(InvalidUniversityStatusException.class,
                () -> universityValidationService.validateUniversity(universidade.getId(), "Reavaliando"));

        // verificacao
        assertEquals("Universidade já avaliada. Situação atual: APPROVED", erro.getMessage());
        verify(universityRepository, never()).save(any(University.class));
        verify(auditService, never()).logUpdate(any(), any(), any(), any());
    }

    // Vialocao nao se valida o que nao existe.

    @Test
    void deveLancarExcecaoQuandoUniversidadeNaoExiste() {
        // preparacao
        UUID id = UUID.randomUUID();
        when(universityRepository.findById(id)).thenReturn(Optional.empty());

        // execucao
        UniversityNotFoundException erro = assertThrows(UniversityNotFoundException.class,
                () -> universityValidationService.validateUniversity(id, "Parecer qualquer"));

        // verificacao
        assertEquals("Universidade não encontrada!", erro.getMessage());
        verify(universityRepository, never()).save(any(University.class));
    }

    //Caso de parecer ausente, vazio ou apenas com espacos, faz recusa antes de qualquer
    // acesso ao repositorio.

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void deveExigirParecerJustificadoNaRecusa(String parecer) {
        // preparacao
        UUID id = UUID.randomUUID();

        // execucao
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> universityValidationService.rejectUniversity(id, parecer));

        // verificacao
        assertEquals("Recusa exige um parecer justificado", erro.getMessage());
        verifyNoInteractions(universityRepository);
        verifyNoInteractions(auditService);
    }


    //Aprovar sem parecer e valido e nao impede a ativacao da universidade.

    @Test
    void deveAprovarSemParecerPorqueAJustificativaSoEObrigatoriaNaRecusa() {
        // preparacao
        University universidade = universidadePendente();
        when(universityRepository.findById(universidade.getId())).thenReturn(Optional.of(universidade));

        // execucao
        UniversityResponseDTO resposta =
                universityValidationService.validateUniversity(universidade.getId(), null);

        // verificacao
        assertEquals(UniversityStatus.APPROVED.name(), resposta.status());
        assertTrue(resposta.active());
        assertTrue(resposta.canReceiveStudents());
        assertNull(resposta.validationNotes());
        verify(universityRepository).save(universidade);
    }
}
