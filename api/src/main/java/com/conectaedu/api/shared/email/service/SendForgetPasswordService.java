package com.conectaedu.api.shared.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendForgetPasswordService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ConectaEdu - Redefinição de Senha");
        message.setText("Você solicitou a redefinição de sua senha no ConectaEdu.\n\n" +
                "Para criar uma nova senha, clique no link abaixo:\n" +
                resetLink + "\n\n" +
                "Este link expirará em 1 hora. Se você não solicitou isso, ignore este e-mail.");

        mailSender.send(message);
    }
}
