package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.ResetPasswordRequest;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmailAndPasswordResetToken(request.email().trim().toLowerCase(), request.code()).orElse(null);

        if (user == null) {
            log.error("Falha no reset de senha: Código inválido para o e-mail {}", request.email());
            auditService.logPasswordResetFail(null, request.email(), "invalid_code");
            throw new BusinessException("Código de recuperação inválido.");
        }

        if (user.isPasswordResetTokenExpired()) {
            log.error("Falha no reset de senha: Token expirado para o usuário: {}", user.getEmail());
            auditService.logPasswordResetFail(user.getId(), user.getEmail(), "expired_token");
            throw new BusinessException("Token de recuperação expirado.");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Senha do usuário {} resetada com sucesso.", user.getEmail());

        auditService.logPasswordResetSuccess(user.getId(), user.getEmail());
    }
}
