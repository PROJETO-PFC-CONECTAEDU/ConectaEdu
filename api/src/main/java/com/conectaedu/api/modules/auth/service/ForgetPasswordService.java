package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.ForgotPasswordRequest;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.email.service.SendForgetPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgetPasswordService {

    private final UserRepository userRepository;
    private final SendForgetPasswordService emailService;
    private final AuditService auditService;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        log.info("Solicitação de recuperação de senha para o e-mail: {}", request.email());

        userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
            user.generatePasswordResetToken();
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getPasswordResetToken());

            auditService.logPasswordResetRequest(user.getId(), user.getEmail());
        }, () -> {
            // E-mail inexistente é registrado como falha para detectar enumeração
            auditService.logPasswordResetFail(null, request.email(), "user_not_found");
        });
    }
}
