package com.conectaedu.api.shared.audit.service;

import com.conectaedu.api.shared.audit.domain.AuditLog;
import com.conectaedu.api.shared.audit.repository.AuditLogRepository;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditAction;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//toda criacao, alteracao e exclusao gera um registro com quem fez e quando.
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final String NOME = "Universidade ABCD";
    private static final String EMAIL = "admin@conectaedu.com";
    private static final int TAMANHO_MAXIMO = 2000;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @AfterEach
    void limparUsuarioAutenticado() {
        SecurityContextHolder.clearContext();
    }

    private void repositorioDevolveORegistroSalvo() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));
    }

    private void autenticar(String email) {
        AuthenticatedUser usuario = new AuthenticatedUser(
                UUID.randomUUID(), email, "senha", null,
                List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }

    private AuditLog registroSalvo() {
        ArgumentCaptor<AuditLog> capturado = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(capturado.capture());
        return capturado.getValue();
    }

    //caminho feliz.
    @Test
    void deveRegistrarCriacaoComOsDadosDaEntidade() {
        UUID id = UUID.randomUUID();
        repositorioDevolveORegistroSalvo();

        auditService.logCreate(AuditEntityType.UNIVERSITY, id, NOME);

        AuditLog registro = registroSalvo();
        assertEquals(AuditEntityType.UNIVERSITY, registro.getEntityType());
        assertEquals(id, registro.getEntityId());
        assertEquals(AuditAction.CREATE, registro.getAction());
        assertEquals("Registro de Universidade \"Universidade ABCD\" foi criado", registro.getDescription());
        assertEquals(LocalDate.now(), registro.getOccurredAt());
    }

    @Test
    void deveRegistrarExclusao() {
        UUID id = UUID.randomUUID();
        repositorioDevolveORegistroSalvo();

        auditService.logDelete(AuditEntityType.SCHOOL, id, "Escola ABCD");

        AuditLog registro = registroSalvo();
        assertEquals(AuditEntityType.SCHOOL, registro.getEntityType());
        assertEquals(id, registro.getEntityId());
        assertEquals(AuditAction.DELETE, registro.getAction());
        assertEquals("Registro de Escola \"Escola ABCD\" foi excluído", registro.getDescription());
    }

    @Test
    void deveRegistrarAlteracaoDescrevendoOQueMudou() {
        UUID id = UUID.randomUUID();
        repositorioDevolveORegistroSalvo();
        AuditDiff alteracoes = AuditDiff.create()
                .field("coordinator", "Joao Paulo", "Maria Sousa")
                .field("requiredInternshipHours", 100, 200);

        auditService.logUpdate(AuditEntityType.UNIVERSITY, id, NOME, alteracoes);

        AuditLog registro = registroSalvo();
        assertEquals(id, registro.getEntityId());
        assertEquals(AuditAction.UPDATE, registro.getAction());
        assertEquals(
                "Registro de Universidade \"Universidade ABCD\" foi alterado: "
                        + "coordinator: de \"Joao Paulo\" para \"Maria Sousa\"; "
                        + "requiredInternshipHours: de \"100\" para \"200\"",
                registro.getDescription());
    }

    @Test
    void naoDeveRegistrarAlteracaoQuandoNadaMudou() {
        AuditDiff alteracoes = AuditDiff.create()
                .field("coordinator", "Joao Paulo", "Joao Paulo");

        auditService.logUpdate(AuditEntityType.UNIVERSITY, UUID.randomUUID(), NOME, alteracoes);

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void deveCortarDescricaoQueUltrapassaOTamanhoMaximo() {
        repositorioDevolveORegistroSalvo();
        String nomeLongo = "A".repeat(2500);

        auditService.logCreate(AuditEntityType.UNIVERSITY, UUID.randomUUID(), nomeLongo);

        AuditLog registro = registroSalvo();
        assertEquals(TAMANHO_MAXIMO, registro.getDescription().length());
        assertTrue(registro.getDescription().startsWith("Registro de Universidade \"AAAA"));
    }

    //o registro guarda o e-mail de quem fez a acao.
    @Test
    void deveAtribuirAcaoAoUsuarioAutenticado() {
        repositorioDevolveORegistroSalvo();
        autenticar(EMAIL);

        auditService.logCreate(AuditEntityType.UNIVERSITY, UUID.randomUUID(), NOME);

        assertEquals(EMAIL, registroSalvo().getPerformedBy());
    }

    @Test
    void deveAtribuirAcaoAoSystemQuandoNaoHaUsuarioAutenticado() {
        repositorioDevolveORegistroSalvo();

        auditService.logCreate(AuditEntityType.LESSON, UUID.randomUUID(), "Aula 1");

        assertEquals("SYSTEM", registroSalvo().getPerformedBy());
    }
}
