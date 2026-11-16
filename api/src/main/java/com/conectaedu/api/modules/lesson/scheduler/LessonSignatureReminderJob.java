package com.conectaedu.api.modules.lesson.scheduler;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.shared.email.service.SendLessonSignatureReminderService;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

//Cinco dias sem assinatura e o diretor é cobrado, o status da aula
//não muda. O reminderSentAt existe para a cobrança sair uma vez, e não a cada execução.

@Component
@RequiredArgsConstructor
public class LessonSignatureReminderJob {

    private static final Logger log = LoggerFactory.getLogger(LessonSignatureReminderJob.class);

    private final LessonRepository lessonRepository;
    private final SendLessonSignatureReminderService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void remindPendingSignatures() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(LessonRules.SIGNATURE_REMINDER_DAYS);

        List<Lesson> pendingSignature = lessonRepository
                .findByStatusAndRegisteredAtBeforeAndReminderSentAtIsNull(LessonStatus.COMPLETED, deadline);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Lesson lesson : pendingSignature) {
            lesson.setReminderSentAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonRepository.save(lesson);

            if (lesson.getDemand().getDirector() != null) {
                emailService.sendSignatureReminderEmail(
                        lesson.getDemand().getDirector().getEmail(),
                        lesson.getDemand().getDirector().getName(),
                        lesson.getSubject(),
                        lesson.getScheduledStart().format(formatter)
                );
            }

            log.info("[LESSON] lembrete de assinatura enviado na aula {}, diretor {}",
                    lesson.getId(),
                    lesson.getDemand().getDirector() == null ? null : lesson.getDemand().getDirector().getId());
        }
    }
}