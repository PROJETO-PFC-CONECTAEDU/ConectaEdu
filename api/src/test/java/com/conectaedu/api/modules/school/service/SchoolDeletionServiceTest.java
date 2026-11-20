package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//so se exclui escola cadastrada e toda exclusao fica registrada na auditoria.

@ExtendWith(MockitoExtension.class)
class SchoolDeletionServiceTest {

    private static final String NOME = "Escola Estadual ABCD";

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private TenancyValidator tenancyValidator;

    @InjectMocks
    private SchoolDeletionService schoolDeletionService;

    private School escolaCadastrada() {
        School escola = new School();
        escola.setId(UUID.randomUUID());
        escola.setName(NOME);
        escola.setCie("123456");
        return escola;
    }

    //caminho feliz quando a escola e removida e a exclusao fica registrada.
    @Test
    void deveExcluirEscolaERegistrarNaAuditoria() {
        School escola = escolaCadastrada();
        escola.setActive(true);
        when(schoolRepository.findById(escola.getId())).thenReturn(Optional.of(escola));

        schoolDeletionService.deleteSchool(escola.getId());

        assertEquals(false, escola.isActive());
        verify(schoolRepository).save(escola);
        verify(auditService).logDelete(AuditEntityType.SCHOOL, escola.getId(), NOME);
    }

    @Test
    void deveLancarExcecaoAoExcluirEscolaInexistente() {
        UUID id = UUID.randomUUID();
        when(schoolRepository.findById(id)).thenReturn(Optional.empty());

        SchoolNotFoundException erro = assertThrows(SchoolNotFoundException.class,
                () -> schoolDeletionService.deleteSchool(id));

        assertEquals("Escola não encontrada!", erro.getMessage());
        verify(schoolRepository, never()).save(any(School.class));
        verifyNoInteractions(auditService);
    }
}
