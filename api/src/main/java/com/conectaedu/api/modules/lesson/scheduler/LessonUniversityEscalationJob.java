package com.conectaedu.api.modules.lesson.scheduler;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//Dez dias sem assinatura e a universidade passa a poder validar a aula no lugar da escola.
// O escalatedAt é o que o LessonSignatureService consulta para liberar o administrador da universidade.

@Component
@RequiredArgsConstructor
public class LessonUniversityEscalationJob {

    private static final Logger log = LoggerFactory.getLogger(LessonUniversityEscalationJob.class);

    private final LessonRepository lessonRepository;

    @Scheduled(cron = "0 30 8 * * *")
    @Transactional
    public void escalateToUniversity() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(LessonRules.ESCALATION_DAYS);

        List<Lesson> stalled = lessonRepository
                .findByStatusAndRegisteredAtBeforeAndEscalatedAtIsNull(LessonStatus.COMPLETED, deadline);

        for (Lesson lesson : stalled) {
            lesson.setEscalatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            lessonRepository.save(lesson);

            log.info("[LESSON] aula {} escalada para a universidade {}", lesson.getId(),
                    lesson.getStudent().getUniversity() == null
                            ? null : lesson.getStudent().getUniversity().getId());
        }
    }
}