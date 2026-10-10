package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.request.UniversityCreationRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityCreationResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.conectaedu.api.shared.exceptions.CnpjAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Regra o CNPJ identifica a instituicao e nao pode se repetir.
//A universidade nasce PENDING e inativa, so o administrador da plataforma faz a ativacao.

@ExtendWith(MockitoExtension.class)
class UniversityCreationServiceTest {

    private static final String CNPJ = "12345671000112";
    private static final String NOME = "Universidade ABCD";

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UniversityCreationService universityCreationService;

    private UniversityCreationRequestDTO request() {
        return new UniversityCreationRequestDTO(
                NOME, CNPJ, "Joao P", "R. teste, 200");
    }

    //Caminho feliz, CNPJ inedito, universidade criada e registrada na auditoria.
    @Test
    void deveCriarUniversidadeQuandoCnpjAindaNaoEstaCadastrado() {
        // preparacao
        UUID idGerado = UUID.randomUUID();
        when(universityRepository.existsByCnpj(CNPJ)).thenReturn(false);
        when(universityRepository.save(any(University.class))).thenAnswer(chamada -> {
            University salva = chamada.getArgument(0);
            salva.setId(idGerado);
            return salva;
        });

        // execucao
        UniversityCreationResponseDTO resposta = universityCreationService.createUniversity(request());

        // verificacao
        assertEquals(idGerado, resposta.id());
        assertEquals("Universidade criada com sucesso!", resposta.message());
        verify(universityRepository).save(any(University.class));
        verify(auditService).logCreate(AuditEntityType.UNIVERSITY, idGerado, NOME);
    }

    //Violacao de regra, CNPJ repetido bloqueia a criacao e nao gera auditoria.
    @Test
    void deveLancarExcecaoQuandoCnpjJaEstaCadastrado() {
        // preparacao
        when(universityRepository.existsByCnpj(CNPJ)).thenReturn(true);

        // execucao
        CnpjAlreadyExistsException erro = assertThrows(CnpjAlreadyExistsException.class,
                () -> universityCreationService.createUniversity(request()));

        // verificacao
        assertEquals("CNPJ já cadastrado!", erro.getMessage());
        verify(universityRepository, never()).save(any(University.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    //Estado inicial da universidade recem-criada, no fluxo de validacao nasce PENDING, inativa
    // e sem data de validacao.

    @Test
    void deveSalvarUniversidadeNovaComoPendenteEInativa() {
        // preparacao
        when(universityRepository.existsByCnpj(anyString())).thenReturn(false);
        ArgumentCaptor<University> capturada = ArgumentCaptor.forClass(University.class);

        // execucao
        universityCreationService.createUniversity(request());

        // verificacao
        verify(universityRepository).save(capturada.capture());
        University salva = capturada.getValue();
        assertEquals(UniversityStatus.PENDING, salva.getStatus());
        assertFalse(salva.isActive());
        assertNull(salva.getValidatedAt());
    }
}
