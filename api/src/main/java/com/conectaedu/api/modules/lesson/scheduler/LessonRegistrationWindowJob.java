package com.conectaedu.api.modules.lesson.scheduler;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//A aula aconteceu, mas o estudante não registrou em 48h depois do fim. A aula não pode ficar
//CONFIRMED para sempre, nem virar hora de estágio sem registro, termina em NOT_COMPLETED.

@Component
@RequiredArgsConstructor
public class LessonRegistrationWindowJob {

    private static final Logger log = LoggerFactory.getLogger(LessonRegistrationWindowJob.class);

    private final LessonRepository lessonRepository;
    private final AuditService auditService;

    @Scheduled(cron = "0 15 * * * *")
    @Transactional
    public void closeRegistrationWindow() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(LessonRules.REGISTRATION_WINDOW_HOURS);

        List<Lesson> unregistered = lessonRepository
                .findByStatusAndScheduledEndBefore(LessonStatus.CONFIRMED, deadline);
        if (unregistered.isEmpty()) {
            return;
        }

        for (Lesson lesson : unregistered) {
            LessonStatus beforeStatus = lesson.getStatus();

            lesson.setStatus(LessonStatus.NOT_COMPLETED);
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonRepository.save(lesson);

            AuditDiff diff = AuditDiff.create().field("status", beforeStatus, lesson.getStatus());
            auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);
        }

        log.info("[LESSON] {} aula(s) marcada(s) como não realizada(s) por falta de registro",
                unregistered.size());
    }
}

