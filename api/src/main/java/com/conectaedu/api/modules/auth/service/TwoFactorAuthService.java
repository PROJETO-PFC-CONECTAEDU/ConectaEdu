package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.*;
import com.conectaedu.api.modules.auth.mapper.LoginResponseMapper;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.email.service.TwoFactorEmailService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import com.conectaedu.api.shared.security.token.GenerateToken;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final UserRepository userRepository;
    private final GenerateToken generateToken;
    private final TwoFactorEmailService emailService;
    private final LoginResponseMapper loginResponseMapper;

    @Transactional
    public TwoFactorSetupResponseDTO setup2FA(User user) {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        user.setSecret2fa(key.getKey());
        userRepository.save(user);

        String qrCodeUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                "ConectaEdu", user.getEmail(), key.getKey(), "ConectaEdu");
        
        return new TwoFactorSetupResponseDTO(qrCodeUrl);
    }

    @Transactional
    public void confirm2FA(User user, int code) {
        if (gAuth.authorize(user.getSecret2fa(), code)) {
            user.set2faEnabled(true);
            userRepository.save(user);
        } else {
            throw new BusinessException("Código 2FA inválido");
        }
    }

    public LoginResponseDTO verify2FA(TwoFactorVerifyRequestDTO data) {
        User userEntity = userRepository.findByEmail(data.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if (gAuth.authorize(userEntity.getSecret2fa(), data.code())) {
            String token = generateToken.generateToken(userEntity);
            return loginResponseMapper.toResponse(userEntity, token);
        }
        throw new BadCredentialsException("Código 2FA inválido");
    }

    @Transactional
    public void requestDisable2FA(User user) {
        String code = String.valueOf((int)((Math.random() * 899999) + 100000));
        user.setDisable2faCode(code);
        userRepository.save(user);
        emailService.sendDisable2FACode(user.getEmail(), code);
    }

    @Transactional
    public void confirmDisable2FA(User user, int code) {
        if (String.valueOf(code).equals(user.getDisable2faCode())) {
            user.set2faEnabled(false);
            user.setSecret2fa(null);
            user.setDisable2faCode(null);
            userRepository.save(user);
        } else {
            throw new BusinessException("Código de desativação inválido");
        }
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
