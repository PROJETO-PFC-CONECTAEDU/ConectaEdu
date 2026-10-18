package com.conectaedu.api.modules.lesson.scheduler;

import com.conectaedu.api.modules.demand.service.DemandUnlinkStudentService;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.event.LessonCancelledEvent;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.CancellationReason;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//O estudante tem o menor entre 3 dias do agendamento e 24h antes da aula para
//confirmar. Passado o prazo a aula cai e a demanda volta para a fila, para o diretor aprovar outro candidato.

@Component
@RequiredArgsConstructor
public class LessonConfirmationExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(LessonConfirmationExpiryJob.class);

    private final LessonRepository lessonRepository;
    private final DemandUnlinkStudentService demandUnlinkStudentService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void expireConfirmations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdBefore = now.minusDays(LessonRules.CONFIRMATION_DEADLINE_DAYS);
        LocalDateTime startingBefore = now.plusHours(LessonRules.CONFIRMATION_CUTOFF_HOURS);

        List<Lesson> expired = lessonRepository.findExpiredConfirmations(
                LessonStatus.SCHEDULED, createdBefore, startingBefore);
        if (expired.isEmpty()) {
            return;
        }

        for (Lesson lesson : expired) {
            LessonStatus beforeStatus = lesson.getStatus();

            lesson.setStatus(LessonStatus.CANCELED);
            lesson.setCancellationReason(CancellationReason.STUDENT_UNAVAILABLE);
            lesson.setCancellationNotes("Cancelada pelo sistema: o estudante não confirmou no prazo.");
            lesson.setUpdatedAt(now);
            lessonRepository.save(lesson);

            AuditDiff diff = AuditDiff.create()
                    .field("status", beforeStatus, lesson.getStatus())
                    .field("motivo_cancelamento", null, lesson.getCancellationReason());
            auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);

            //A demanda já tem o desvínculo pronto, ele limpa o estudante, volta o status para
            //WAITING e fecha o histórico.

            if (lesson.getDemand().getStudent() != null) {
                demandUnlinkStudentService.unlink(lesson.getDemand().getId());
            }

            eventPublisher.publishEvent(new LessonCancelledEvent(
                    lesson.getId(),
                    lesson.getStudent().getId(),
                    lesson.getDemand().getDirector() == null ? null : lesson.getDemand().getDirector().getId(),
                    CancellationReason.STUDENT_UNAVAILABLE,
                    true));
        }

        log.info("[LESSON] {} aula(s) cancelada(s) por falta de confirmação", expired.size());
    }
}
