package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolCreationRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolCreationResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.SchoolStatus;
import com.conectaedu.api.shared.exceptions.CIEAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolCreationServiceTest {

    private static final String CIE = "123456";
    private static final String NOME = "Escola Estadual ABCD";
    private static final String DIRETOR = "Maria Sousa";
    private static final String ENDERECO = "R. teste, 100";

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SchoolCreationService schoolCreationService;

    private SchoolCreationRequestDTO request() {
        return new SchoolCreationRequestDTO(
                NOME, CIE, DIRETOR, ENDERECO, -23.5505, -46.6333);
    }

    //caminho feliz, CIE não utilizado, escola criada e registrada.
    @Test
    void deveCriarEscolaQuandoCieAindaNaoEstaCadastrado() {
        UUID idGerado = UUID.randomUUID();
        when(schoolRepository.existsByCie(CIE)).thenReturn(false);
        when(schoolRepository.save(any(School.class))).thenAnswer(chamada -> {
            School salva = chamada.getArgument(0);
            salva.setId(idGerado);
            return salva;
        });

        SchoolCreationResponseDTO resposta = schoolCreationService.createSchool(request());

        assertEquals("Escola cadastrada com sucesso!", resposta.message());
        verify(schoolRepository).save(any(School.class));
        verify(auditService).logCreate(AuditEntityType.SCHOOL, idGerado, NOME);
    }

    //quebra de regra quando CIE repetido bloqueia a criacao e nao gera auditoria.
    @Test
    void deveLancarExcecaoQuandoCieJaEstaCadastrado() {
        when(schoolRepository.existsByCie(CIE)).thenReturn(true);

        CIEAlreadyExistsException erro = assertThrows(CIEAlreadyExistsException.class,
                () -> schoolCreationService.createSchool(request()));

        assertEquals("CIE já cadastrado", erro.getMessage());
        verify(schoolRepository, never()).save(any(School.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    //primeiro estagio da escola recem-criada, nasce PENDING_ACTIVATION, inativa,sem data de validacao e com os dados do cadastro copiados.
    @Test
    void deveSalvarEscolaNovaComoPendenteEInativa() {
        when(schoolRepository.existsByCie(anyString())).thenReturn(false);
        ArgumentCaptor<School> capturada = ArgumentCaptor.forClass(School.class);

        schoolCreationService.createSchool(request());

        verify(schoolRepository).save(capturada.capture());
        School salva = capturada.getValue();
        assertEquals(SchoolStatus.PENDING_ACTIVATION, salva.getStatus());
        assertFalse(salva.isActive());
        assertNull(salva.getValidatedAt());
        assertNotNull(salva.getCreatedAt());
        assertEquals(NOME, salva.getName());
        assertEquals(CIE, salva.getCie());
        assertEquals(DIRETOR, salva.getDirector());
        assertEquals(ENDERECO, salva.getAddress());
        assertEquals(-23.5505, salva.getLatitude());
        assertEquals(-46.6333, salva.getLongitude());
    }

    //coordenadas sao opcionais, se criada a escola sem e salva sem localizacao.
    @Test
    void deveCriarEscolaSemCoordenadasQuandoNaoInformadas() {
        when(schoolRepository.existsByCie(CIE)).thenReturn(false);
        ArgumentCaptor<School> capturada = ArgumentCaptor.forClass(School.class);

        schoolCreationService.createSchool(
                new SchoolCreationRequestDTO(NOME, CIE, DIRETOR, ENDERECO, null, null));

        verify(schoolRepository).save(capturada.capture());
        School salva = capturada.getValue();
        assertEquals(0.0, salva.getLatitude());
        assertEquals(0.0, salva.getLongitude());
    }
}
