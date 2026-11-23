package com.conectaedu.api.modules.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.conectaedu.api.modules.auth.domain.RevokedToken;
import com.conectaedu.api.modules.auth.repository.TokenRepository;
import com.conectaedu.api.shared.security.token.ValidateToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//regras, o logout revoga o token pelo seu identificador ate a data de expiracao,
//token invalido ou ja revogado nao gera novo registro.

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    private static final String TOKEN = "token-jwt-de-teste";
    private static final String TOKEN_ID = "9b1d4c7a-2e5f-4a8b-b3c6-0f7e1d2a5c84";

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private ValidateToken validateToken;

    @Mock
    private DecodedJWT decodedJWT;

    @InjectMocks
    private LogoutService logoutService;

    //caminho feliz, token valido e ainda nao revogado e registrado com identificador e expiracao.
    @Test
    void deveRevogarTokenQuandoValidoEAindaNaoRevogado() {
        Instant expiracao = Instant.now().plusSeconds(3600);
        when(validateToken.validateToken(TOKEN)).thenReturn(decodedJWT);
        when(decodedJWT.getId()).thenReturn(TOKEN_ID);
        when(decodedJWT.getExpiresAtAsInstant()).thenReturn(expiracao);
        when(tokenRepository.existsByTokenId(TOKEN_ID)).thenReturn(false);
        ArgumentCaptor<RevokedToken> capturado = ArgumentCaptor.forClass(RevokedToken.class);

        logoutService.logout(TOKEN);

        verify(tokenRepository).save(capturado.capture());
        RevokedToken revogado = capturado.getValue();
        assertEquals(TOKEN_ID, revogado.getTokenId());
        assertEquals(expiracao, revogado.getExpiresAt());
        assertNotNull(revogado.getRevokedAt());
    }

    @Test
    void deveIgnorarQuandoTokenEInvalido() {
        when(validateToken.validateToken(TOKEN)).thenReturn(null);

        assertDoesNotThrow(() -> logoutService.logout(TOKEN));

        verify(tokenRepository, never()).existsByTokenId(anyString());
        verify(tokenRepository, never()).save(any(RevokedToken.class));
    }

    //logout repetido, token ja revogado nao e registrado duas vezes.
    @Test
    void naoDeveDuplicarRegistroQuandoTokenJaFoiRevogado() {
        when(validateToken.validateToken(TOKEN)).thenReturn(decodedJWT);
        when(decodedJWT.getId()).thenReturn(TOKEN_ID);
        when(tokenRepository.existsByTokenId(TOKEN_ID)).thenReturn(true);

        logoutService.logout(TOKEN);

        verify(tokenRepository, never()).save(any(RevokedToken.class));
    }

    @Test
    void naoDeveRevogarQuandoTokenNaoTemIdentificador() {
        when(validateToken.validateToken(TOKEN)).thenReturn(decodedJWT);
        when(decodedJWT.getId()).thenReturn(null);

        logoutService.logout(TOKEN);

        verify(tokenRepository, never()).existsByTokenId(any());
        verify(tokenRepository, never()).save(any(RevokedToken.class));
    }
}
