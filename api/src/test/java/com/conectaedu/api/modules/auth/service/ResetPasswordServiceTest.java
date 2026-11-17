package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.ResetPasswordRequest;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//regras, a senha so e trocada com token existente e dentro da validade, o token e de uso unico e toda tentativa, com sucesso ou nao, e auditada.

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    private static final String EMAIL = "maria.sousa@conectaedu.com";
    private static final String TOKEN = "3f2b8c1e-6a4d-4f0b-9c7e-2d5a1b8e4f60";
    private static final String NOVA_SENHA = "NovaSenha@123";
    private static final String SENHA_ANTIGA_CODIFICADA = "hash-da-senha-antiga";
    private static final String NOVA_SENHA_CODIFICADA = "hash-da-senha-nova";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    private ResetPasswordRequest request() {
        return new ResetPasswordRequest(TOKEN, NOVA_SENHA);
    }

    private User usuario(UUID id, LocalDateTime validadeDoToken) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setName("Maria Sousa");
        usuario.setEmail(EMAIL);
        usuario.setPassword(SENHA_ANTIGA_CODIFICADA);
        usuario.setPasswordResetToken(TOKEN);
        usuario.setPasswordResetExpiresAt(validadeDoToken);
        return usuario;
    }

    //caminho feliz, token valido troca a senha pela versao codificada, invalida o token e audita o sucesso.
    @Test
    void deveRedefinirSenhaQuandoTokenEValido() {
        UUID usuarioId = UUID.randomUUID();
        User usuario = usuario(usuarioId, LocalDateTime.now().plusMinutes(30));
        when(userRepository.findByPasswordResetToken(TOKEN)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(NOVA_SENHA)).thenReturn(NOVA_SENHA_CODIFICADA);

        resetPasswordService.resetPassword(request(), httpRequest);

        assertEquals(NOVA_SENHA_CODIFICADA, usuario.getPassword());
        assertNull(usuario.getPasswordResetToken());
        assertNull(usuario.getPasswordResetExpiresAt());
        assertNotNull(usuario.getUpdatedAt());
        verify(userRepository).save(usuario);
        verify(auditService).logPasswordResetSuccess(usuarioId, EMAIL);
        verify(auditService, never()).logPasswordResetFail(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoTokenNaoExiste() {
        when(userRepository.findByPasswordResetToken(TOKEN)).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class,
                () -> resetPasswordService.resetPassword(request(), httpRequest));

        assertEquals("Token de recuperação inválido.", erro.getMessage());
        verify(auditService).logPasswordResetFail(null, null, "invalid_token");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logPasswordResetSuccess(any(), any());
    }

    //token vencido, falha auditada com o usuario e a senha antiga e mantida.
    @Test
    void deveLancarExcecaoQuandoTokenEstaExpirado() {
        UUID usuarioId = UUID.randomUUID();
        User usuario = usuario(usuarioId, LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByPasswordResetToken(TOKEN)).thenReturn(Optional.of(usuario));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> resetPasswordService.resetPassword(request(), httpRequest));

        assertEquals("Token de recuperação expirado.", erro.getMessage());
        assertEquals(SENHA_ANTIGA_CODIFICADA, usuario.getPassword());
        verify(auditService).logPasswordResetFail(usuarioId, EMAIL, "expired_token");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logPasswordResetSuccess(any(), any());
    }
}
