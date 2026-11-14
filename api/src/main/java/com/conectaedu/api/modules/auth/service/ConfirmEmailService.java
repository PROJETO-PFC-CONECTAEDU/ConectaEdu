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
    public void confirmEmail(String email, String code) {
        User user = userRepository.findByEmailAndEmailConfirmationToken(email, code).orElse(null);

        if (user == null) {
            log.error("Falha na confirmação de e-mail: Código inválido para o e-mail {}", email);
            auditService.logEmailConfirmationFail(code, "invalid_code");
            throw new BusinessException("Código de confirmação inválido.");
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
