package com.conectaedu.api.modules.feedback.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.domain.DemandStudent;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import com.conectaedu.api.modules.feedback.dto.request.ClassEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.mapper.FeedbackMapper;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.EvaluationAlreadySubmittedException;
import com.conectaedu.api.shared.exceptions.InvalidDemandStatusException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassEvaluationService {

    private final DemandRepository demandRepository;
    private final ClassEvaluationRepository classEvaluationRepository;
    private final FeedbackMapper feedbackMapper;
    private final AuditService auditService;

    //o estudante que atendeu a necessidade faz o balanço da sala ao fim do ciclo.
    @Transactional
    public ClassEvaluationResponseDTO evaluate(ClassEvaluationRequestDTO request) {
        UUID studentId = SecurityContextHolder.getAuthenticatedUserId();

        Demand demand = demandRepository.findById(request.demandId())
                .orElseThrow(() -> new DemandNotFoundException("Necessidade não encontrada!"));

        if (demand.getStatus() != DemandStatus.FINISHED) {
            throw new InvalidDemandStatusException(
                    "A Necessidade está com status %s e só pode ser avaliada depois de concluída."
                            .formatted(demand.getStatus()));
        }

        //vale para quem passou pela necessidade, mesmo que tenha sido desvinculado antes do fim.
        Student student = demand.getStudentHistory().stream()
                .map(DemandStudent::getStudent)
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException(
                        "Apenas o estudante que atendeu a necessidade pode avaliar a sala."));

        //a avaliação não se edita, só se complementa em ciclo posterior.
        if (classEvaluationRepository.existsByDemand_IdAndStudent_Id(demand.getId(), studentId)) {
            throw new EvaluationAlreadySubmittedException(
                    "Você já avaliou a sala desta Necessidade.");
        }

        ClassEvaluation evaluation = new ClassEvaluation();
        evaluation.setDemand(demand);
        evaluation.setStudent(student);
        evaluation.setEngagement(request.engagement());
        evaluation.setProgress(request.progress());
        evaluation.setInfrastructure(request.infrastructure());
        evaluation.setComments(request.comments());
        evaluation.setCreatedAt(LocalDateTime.now());

        classEvaluationRepository.save(evaluation);

        auditService.logCreate(AuditEntityType.EVALUATION, evaluation.getId(), evaluation.getAuditName());

        return feedbackMapper.mapToResponseDTO(evaluation);
    }
}
