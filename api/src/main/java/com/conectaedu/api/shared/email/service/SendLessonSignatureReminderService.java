package com.conectaedu.api.shared.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendLessonSignatureReminderService {

    private final JavaMailSender mailSender;

    public void sendSignatureReminderEmail(String to, String directorName, String subject, String date) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ConectaEdu - Lembrete de Assinatura de Aula");
        message.setText("Olá, " + directorName + ",\n\n" +
                "Identificamos que a aula de " + subject + " realizada em " + date + " ainda não foi assinada.\n\n" +
                "Por favor, acesse a plataforma ConectaEdu para realizar a assinatura e validar as horas do estudante.\n\n" +
                "Atenciosamente,\n" +
                "Equipe ConectaEdu");

        mailSender.send(message);
    }
}
