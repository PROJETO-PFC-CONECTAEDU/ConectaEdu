package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.auth.dto.TwoFactorSetupResponseDTO;
import com.conectaedu.api.modules.auth.dto.TwoFactorVerifyRequestDTO;
import com.conectaedu.api.modules.auth.mapper.LoginResponseMapper;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.email.service.TwoFactorEmailService;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.BusinessException;
import com.conectaedu.api.shared.security.token.GenerateToken;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Regras, o 2FA so e ativado depois de confirmado com um codigo do autenticador,
//o token de login so sai com codigo valido e a desativacao exige o codigo enviado por e-mail.
//O GoogleAuthenticator e criado dentro da service e nao pode ser trocado por mock,
//entao os codigos validos sao calculados aqui a partir do mesmo segredo.

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {

    private static final String EMAIL = "maria.sousa@conectaedu.com";
    private static final String TOKEN = "token-jwt-de-teste";
    private static final int CODIGO_DESATIVACAO = 654321;
    //codigo fora da faixa aceita pelo autenticador, nunca coincide com um codigo valido.
    private static final int CODIGO_INVALIDO = -1;

    private final GoogleAuthenticator autenticador = new GoogleAuthenticator();

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenerateToken generateToken;

    @Mock
    private TwoFactorEmailService emailService;

    @Mock
    private LoginResponseMapper loginResponseMapper;

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    private User usuario() {
        User usuario = new User();
        usuario.setId(UUID.randomUUID());
        usuario.setName("Maria Sousa");
        usuario.setEmail(EMAIL);
        usuario.setUserRole(UserRole.SCHOOL_DIRECTOR);
        return usuario;
    }

    private User usuarioComSegredo() {
        User usuario = usuario();
        usuario.setSecret2fa(autenticador.createCredentials().getKey());
        return usuario;
    }

    private int codigoValido(User usuario) {
        return autenticador.getTotpPassword(usuario.getSecret2fa());
    }

    //a configuracao grava o segredo no usuario e devolve a URL do QR code, sem ativar o 2FA ainda.
    @Test
    void deveGerarSegredoEUrlDoQrCodeAoConfigurar() {
        User usuario = usuario();

        TwoFactorSetupResponseDTO resposta = twoFactorAuthService.setup2FA(usuario);

        assertNotNull(usuario.getSecret2fa());
        assertFalse(usuario.is2faEnabled());
        assertEquals("otpauth://totp/ConectaEdu:" + EMAIL + "?secret=" + usuario.getSecret2fa()
                + "&issuer=ConectaEdu", resposta.qrCodeUrl());
        verify(userRepository).save(usuario);
    }

    //caminho feliz.
    @Test
    void deveAtivarDoisFatoresQuandoCodigoEValido() {
        User usuario = usuarioComSegredo();

        twoFactorAuthService.confirm2FA(usuario, codigoValido(usuario));

        assertTrue(usuario.is2faEnabled());
        verify(userRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoAoConfirmarComCodigoInvalido() {
        User usuario = usuarioComSegredo();

        BusinessException erro = assertThrows(BusinessException.class,
                () -> twoFactorAuthService.confirm2FA(usuario, CODIGO_INVALIDO));

        assertEquals("Código 2FA inválido", erro.getMessage());
        assertFalse(usuario.is2faEnabled());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRetornarTokenQuandoCodigoDeVerificacaoEValido() {
        User usuario = usuarioComSegredo();
        LoginResponseDTO esperado = new LoginResponseDTO();
        esperado.setToken(TOKEN);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(generateToken.generateToken(usuario)).thenReturn(TOKEN);
        when(loginResponseMapper.toResponse(usuario, TOKEN)).thenReturn(esperado);

        LoginResponseDTO resposta = twoFactorAuthService.verify2FA(
                new TwoFactorVerifyRequestDTO(EMAIL, codigoValido(usuario)));

        assertSame(esperado, resposta);
    }

    @Test
    void deveLancarExcecaoQuandoCodigoDeVerificacaoEInvalido() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioComSegredo()));

        BadCredentialsException erro = assertThrows(BadCredentialsException.class,
                () -> twoFactorAuthService.verify2FA(new TwoFactorVerifyRequestDTO(EMAIL, CODIGO_INVALIDO)));

        assertEquals("Código 2FA inválido", erro.getMessage());
        verify(generateToken, never()).generateToken(any(User.class));
    }

    //verificacao para um e-mail que nao existe.
    @Test
    void deveLancarExcecaoAoVerificarQuandoUsuarioNaoExiste() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UsernameNotFoundException erro = assertThrows(UsernameNotFoundException.class,
                () -> twoFactorAuthService.verify2FA(new TwoFactorVerifyRequestDTO(EMAIL, 123456)));

        assertEquals("Usuário não encontrado", erro.getMessage());
        verify(generateToken, never()).generateToken(any(User.class));
    }

    //o pedido de desativacao grava um codigo de 6 digitos e envia o mesmo codigo por e-mail.
    @Test
    void deveGerarCodigoEEnviarEmailAoSolicitarDesativacao() {
        User usuario = usuarioComSegredo();
        usuario.set2faEnabled(true);
        ArgumentCaptor<String> codigoEnviado = ArgumentCaptor.forClass(String.class);

        twoFactorAuthService.requestDisable2FA(usuario);

        verify(emailService).sendDisable2FACode(eq(EMAIL), codigoEnviado.capture());
        assertTrue(codigoEnviado.getValue().matches("\\d{6}"));
        assertEquals(codigoEnviado.getValue(), usuario.getDisable2faCode());
        assertTrue(usuario.is2faEnabled());
        verify(userRepository).save(usuario);
    }

    //caminho feliz da desativacao, codigo confere e o 2FA, o segredo e o codigo sao limpos.
    @Test
    void deveDesativarDoisFatoresQuandoCodigoDeDesativacaoConfere() {
        User usuario = usuarioComSegredo();
        usuario.set2faEnabled(true);
        usuario.setDisable2faCode(String.valueOf(CODIGO_DESATIVACAO));

        twoFactorAuthService.confirmDisable2FA(usuario, CODIGO_DESATIVACAO);

        assertFalse(usuario.is2faEnabled());
        assertNull(usuario.getSecret2fa());
        assertNull(usuario.getDisable2faCode());
        verify(userRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoCodigoDeDesativacaoNaoConfere() {
        User usuario = usuarioComSegredo();
        usuario.set2faEnabled(true);
        usuario.setDisable2faCode(String.valueOf(CODIGO_DESATIVACAO));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> twoFactorAuthService.confirmDisable2FA(usuario, 111111));

        assertEquals("Código de desativação inválido", erro.getMessage());
        assertTrue(usuario.is2faEnabled());
        assertNotNull(usuario.getSecret2fa());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendDisable2FACode(anyString(), anyString());
    }
}
