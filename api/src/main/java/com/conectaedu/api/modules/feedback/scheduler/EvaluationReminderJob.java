package com.conectaedu.api.modules.feedback.scheduler;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.feedback.support.FeedbackRules;
import com.conectaedu.api.shared.enums.DemandStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EvaluationReminderJob {

    private static final Logger log = LoggerFactory.getLogger(EvaluationReminderJob.class);

    private final DemandRepository demandRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void remindPendingEvaluations() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(FeedbackRules.REMINDER_DAYS);
        LocalDateTime windowStart = deadline.minusDays(1);

        List<Demand> finished = demandRepository.findByStatusAndFinishedAtBefore(DemandStatus.FINISHED, deadline);

        for (Demand demand : finished) {
            if (demand.getFinishedAt().isBefore(windowStart)) {
                continue;
            }

            if (studentEvaluationRepository.existsByDemand_Id(demand.getId())) {
                continue;
            }

            log.info("[FEEDBACK] avaliação pendente há {} dias na Necessidade {}, diretor {}",
                    FeedbackRules.REMINDER_DAYS, demand.getId(),
                    demand.getDirector() == null ? null : demand.getDirector().getId());
        }
    }
}
