package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Regras, o e-mail so e confirmado com o codigo enviado para ele, o codigo e de uso unico e confirmar de novo nao altera nada.

@ExtendWith(MockitoExtension.class)
class ConfirmEmailServiceTest {

    private static final String EMAIL = "maria.sousa@conectaedu.com";
    private static final String CODIGO = "482913";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ConfirmEmailService confirmEmailService;

    private User usuario(UUID id, boolean emailConfirmado) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setName("Maria Sousa");
        usuario.setEmail(EMAIL);
        usuario.setEmailConfirmationToken(CODIGO);
        usuario.setEmailConfirmed(emailConfirmado);
        return usuario;
    }

    //caminho feliz.
    @Test
    void deveConfirmarEmailQuandoCodigoEValido() {
        UUID usuarioId = UUID.randomUUID();
        User usuario = usuario(usuarioId, false);
        when(userRepository.findByEmailAndEmailConfirmationToken(EMAIL, CODIGO))
                .thenReturn(Optional.of(usuario));

        confirmEmailService.confirmEmail(EMAIL, CODIGO);

        assertTrue(usuario.isEmailConfirmed());
        assertNull(usuario.getEmailConfirmationToken());
        assertNotNull(usuario.getUpdatedAt());
        verify(userRepository).save(usuario);
        verify(auditService).logEmailConfirmationSuccess(usuarioId, EMAIL);
        verify(auditService, never()).logEmailConfirmationFail(any(), any());
    }

    //codigo que nao pertence ao e-mail, falha auditada e nada e salvo.
    @Test
    void deveLancarExcecaoQuandoCodigoEInvalido() {
        when(userRepository.findByEmailAndEmailConfirmationToken(EMAIL, CODIGO))
                .thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class,
                () -> confirmEmailService.confirmEmail(EMAIL, CODIGO));

        assertEquals("Código de confirmação inválido.", erro.getMessage());
        verify(auditService).logEmailConfirmationFail(CODIGO, "invalid_code");
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logEmailConfirmationSuccess(any(), any());
    }

    @Test
    void deveIgnorarQuandoEmailJaEstaConfirmado() {
        User usuario = usuario(UUID.randomUUID(), true);
        when(userRepository.findByEmailAndEmailConfirmationToken(EMAIL, CODIGO))
                .thenReturn(Optional.of(usuario));

        assertDoesNotThrow(() -> confirmEmailService.confirmEmail(EMAIL, CODIGO));

        assertTrue(usuario.isEmailConfirmed());
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logEmailConfirmationSuccess(any(), any());
        verify(auditService, never()).logEmailConfirmationFail(any(), any());
    }
}
