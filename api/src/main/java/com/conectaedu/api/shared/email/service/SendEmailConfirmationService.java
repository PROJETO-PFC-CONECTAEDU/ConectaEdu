package com.conectaedu.api.shared.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendEmailConfirmationService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendConfirmationEmail(String to, String token) {
        String confirmationLink = frontendUrl + "/auth/confirm-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ConectaEdu - Confirmação de E-mail");
        message.setText("Bem-vindo ao ConectaEdu!\n\n" +
                "Para confirmar seu e-mail e ativar sua conta, clique no link abaixo:\n" +
                confirmationLink + "\n\n" +
                "Se você não se cadastrou no ConectaEdu, ignore este e-mail.");

        mailSender.send(message);
    }
}
