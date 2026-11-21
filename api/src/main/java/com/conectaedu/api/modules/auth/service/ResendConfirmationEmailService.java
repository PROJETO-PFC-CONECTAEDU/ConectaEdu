package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.email.service.SendEmailConfirmationService;
import com.conectaedu.api.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResendConfirmationEmailService {

    private final UserRepository userRepository;
    private final SendEmailConfirmationService emailConfirmationService;

    @Transactional
    public void resend(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        if (user.isEmailConfirmed()) {
            throw new BusinessException("E-mail já confirmado.");
        }

        if (!user.canResendEmailConfirmation()) {
            throw new BusinessException("Aguarde um minuto antes de solicitar um novo e-mail.");
        }

        user.generateEmailConfirmationToken();
        userRepository.save(user);

        emailConfirmationService.sendConfirmationEmail(user.getEmail(), user.getEmailConfirmationToken());
    }
}
