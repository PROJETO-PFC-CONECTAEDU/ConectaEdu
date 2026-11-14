package com.conectaedu.api.shared.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwoFactorEmailService {

    private final JavaMailSender mailSender;

    public void sendDisable2FACode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ConectaEdu - Código de Desativação do 2FA");
        message.setText("Você solicitou a desativação da autenticação de dois fatores.\n\n" +
                "Seu código de desativação é: " + code + "\n\n" +
                "Se você não solicitou isso, mude sua senha imediatamente.");

        mailSender.send(message);
    }
}
