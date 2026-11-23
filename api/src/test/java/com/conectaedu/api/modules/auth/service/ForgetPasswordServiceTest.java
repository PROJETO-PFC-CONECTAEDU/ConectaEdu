package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.ForgotPasswordRequest;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.email.service.SendForgetPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//regras, o pedido de recuperacao nunca revela se o e-mail existe, mas a tentativa com e-mail desconhecido fica registrada na auditoria.

@ExtendWith(MockitoExtension.class)
class ForgetPasswordServiceTest {

    private static final String EMAIL = "maria.sousa@conectaedu.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private SendForgetPasswordService emailService;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ForgetPasswordService forgetPasswordService;

    private ForgotPasswordRequest request() {
        return new ForgotPasswordRequest(EMAIL);
    }

    private User usuario(UUID id) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setName("Maria Sousa");
        usuario.setEmail(EMAIL);
        return usuario;
    }

    //caminho feliz, gera o token com validade, salva, envia o e-mail e audita o pedido.
    @Test
    void deveGerarTokenEEnviarEmailQuandoUsuarioExiste() {
        UUID usuarioId = UUID.randomUUID();
        User usuario = usuario(usuarioId);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        forgetPasswordService.forgotPassword(request(), httpRequest);

        assertNotNull(usuario.getPasswordResetToken());
        assertTrue(usuario.getPasswordResetExpiresAt().isAfter(LocalDateTime.now()));
        verify(userRepository).save(usuario);
        verify(emailService).sendPasswordResetEmail(EMAIL, usuario.getPasswordResetToken());
        verify(auditService).logPasswordResetRequest(usuarioId, EMAIL);
        verify(auditService, never()).logPasswordResetFail(any(), any(), any());
    }

    //erro apenas na falha auditada para detectar enumeracao.
    @Test
    void deveApenasAuditarFalhaQuandoEmailNaoExiste() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> forgetPasswordService.forgotPassword(request(), httpRequest));

        verify(auditService).logPasswordResetFail(null, EMAIL, "user_not_found");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        verify(auditService, never()).logPasswordResetRequest(any(), any());
    }
}
