package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmEmailService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public void confirmEmail(String token) {
        User user = userRepository.findByEmailConfirmationToken(token).orElse(null);

        if (user == null) {
            log.error("Falha na confirmação de e-mail: Token inválido {}", token);
            auditService.logEmailConfirmationFail(token, "invalid_token");
            throw new BusinessException("Token de confirmação inválido.");
        }

        if (user.isEmailConfirmed()) {
            log.warn("E-mail já confirmado para o usuário: {}", user.getEmail());
            return;
        }

        user.confirmEmail();
        userRepository.save(user);
        log.info("E-mail do usuário {} confirmado com sucesso.", user.getEmail());

        auditService.logEmailConfirmationSuccess(user.getId(), user.getEmail());
    }
}
