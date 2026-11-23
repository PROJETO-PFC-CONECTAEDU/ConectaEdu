package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.LoginRequestDTO;
import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.auth.mapper.LoginResponseMapper;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.BusinessException;
import com.conectaedu.api.shared.security.token.GenerateToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//regras, so recebe token quem tem credenciais validas e e-mail confirmado,
//com 2FA ativo o token so sai depois da verificacao do codigo.

@ExtendWith(MockitoExtension.class)
class AuthLoginServiceTest {

    private static final String EMAIL = "maria.sousa@conectaedu.com";
    private static final String SENHA = "Senha@123";
    private static final String TOKEN = "token-jwt-de-teste";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenerateToken generateToken;

    @Mock
    private LoginResponseMapper loginResponseMapper;

    @InjectMocks
    private AuthLoginService authLoginService;

    private LoginRequestDTO request() {
        return new LoginRequestDTO(EMAIL, SENHA);
    }

    private User usuario(boolean emailConfirmado, boolean doisFatoresAtivo) {
        User usuario = new User();
        usuario.setId(UUID.randomUUID());
        usuario.setName("Maria Sousa");
        usuario.setEmail(EMAIL);
        usuario.setUserRole(UserRole.SCHOOL_DIRECTOR);
        usuario.setEmailConfirmed(emailConfirmado);
        usuario.set2faEnabled(doisFatoresAtivo);
        return usuario;
    }

    //caminho feliz
    @Test
    void deveRetornarTokenQuandoCredenciaisSaoValidas() {
        User usuario = usuario(true, false);
        LoginResponseDTO esperado = new LoginResponseDTO();
        esperado.setToken(TOKEN);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(generateToken.generateToken(usuario)).thenReturn(TOKEN);
        when(loginResponseMapper.toResponse(usuario, TOKEN)).thenReturn(esperado);
        ArgumentCaptor<Authentication> capturada = ArgumentCaptor.forClass(Authentication.class);

        LoginResponseDTO resposta = authLoginService.login(request());

        assertSame(esperado, resposta);
        verify(authenticationManager).authenticate(capturada.capture());
        assertTrue(capturada.getValue() instanceof UsernamePasswordAuthenticationToken);
        assertEquals(EMAIL, capturada.getValue().getPrincipal());
        assertEquals(SENHA, capturada.getValue().getCredentials());
    }

    //credenciais invalidas, a falha do AuthenticationManager interrompe o login antes de qualquer consulta.
    @Test
    void devePropagarExcecaoQuandoCredenciaisSaoInvalidas() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(BadCredentialsException.class, () -> authLoginService.login(request()));

        verify(userRepository, never()).findByEmail(anyString());
        verify(generateToken, never()).generateToken(any(User.class));
    }

    //usuario autenticado mas nao localizado no repositorio.
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UsernameNotFoundException erro = assertThrows(UsernameNotFoundException.class,
                () -> authLoginService.login(request()));

        assertEquals("Usuário não encontrado", erro.getMessage());
        verify(generateToken, never()).generateToken(any(User.class));
    }

    //e-mail nao confirmado bloqueia o login e nao gera token.
    @Test
    void deveLancarExcecaoQuandoEmailNaoFoiConfirmado() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(false, false)));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> authLoginService.login(request()));

        assertEquals("E-mail não confirmado. Verifique sua caixa de entrada.", erro.getMessage());
        verify(generateToken, never()).generateToken(any(User.class));
    }

    //com 2FA ativo o login so sinaliza a segunda etapa, o token fica para a verificacao do codigo.
    @Test
    void deveRetornarRespostaSemTokenQuandoDoisFatoresEstaAtivo() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true, true)));

        LoginResponseDTO resposta = authLoginService.login(request());

        assertEquals(EMAIL, resposta.getEmail());
        assertTrue(resposta.isTwoFactorEnabled());
        assertNull(resposta.getToken());
        verify(generateToken, never()).generateToken(any(User.class));
        verify(loginResponseMapper, never()).toResponse(any(User.class), anyString());
    }
}
