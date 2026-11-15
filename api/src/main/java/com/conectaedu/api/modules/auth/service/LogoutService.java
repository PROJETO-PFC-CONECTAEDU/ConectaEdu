package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.domain.RevokedToken;
import com.conectaedu.api.modules.auth.repository.TokenRepository;
import com.conectaedu.api.shared.security.token.ValidateToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenRepository tokenRepository;
    private final ValidateToken validateToken;

    public void logout(String token) {
        var decodedJWT = validateToken.validateToken(token);
        if (decodedJWT == null) {
            return;
        }

        String tokenId = decodedJWT.getId();
        Instant expiresAt = decodedJWT.getExpiresAtAsInstant();

        if (tokenId != null && !tokenRepository.existsByTokenId(tokenId)) {
            RevokedToken revokedToken = new RevokedToken();
            revokedToken.setTokenId(tokenId);
            revokedToken.setExpiresAt(expiresAt);
            revokedToken.setRevokedAt(Instant.now());
            tokenRepository.save(revokedToken);
        }
    }
}
