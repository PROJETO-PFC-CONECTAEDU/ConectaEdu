package com.conectaedu.api.shared.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendForgetPasswordService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ConectaEdu - Redefinição de Senha");
        message.setText("Você solicitou a redefinição de sua senha no ConectaEdu.\n\n" +
                "Use o código abaixo para criar uma nova senha:\n\n" +
                token + "\n\n" +
                "Este código expirará em 1 hora. Se você não solicitou isso, ignore este e-mail.");

        mailSender.send(message);
    }
}
