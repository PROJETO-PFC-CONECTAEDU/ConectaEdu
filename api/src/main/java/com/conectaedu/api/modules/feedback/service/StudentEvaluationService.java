package com.conectaedu.api.modules.feedback.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.domain.DemandStudent;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.feedback.dto.request.StudentEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.mapper.FeedbackMapper;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
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
public class StudentEvaluationService {

    private final DemandRepository demandRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final FeedbackMapper feedbackMapper;
    private final AuditService auditService;

    //o diretor responsável pela necessidade avalia o estudante RN-46. A avaliação é puxada e não depende do DemandFinishedEvent, só do status FINISHED.
    @Transactional
    public StudentEvaluationResponseDTO evaluate(StudentEvaluationRequestDTO request) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();

        Demand demand = demandRepository.findById(request.demandId())
                .orElseThrow(() -> new DemandNotFoundException("Necessidade não encontrada!"));

        if (demand.getStatus() != DemandStatus.FINISHED) {
            throw new InvalidDemandStatusException(
                    "A Necessidade está com status %s e só pode ser avaliada depois de concluída."
                            .formatted(demand.getStatus()));
        }

        if (!demand.getDirector().getId().equals(directorId)) {
            throw new AccessDeniedException("Apenas o diretor responsável pela Necessidade pode avaliá-la.");
        }

        Student student = demand.getStudentHistory().stream()
                .map(DemandStudent::getStudent)
                .filter(s -> s.getId().equals(request.studentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O estudante informado não atendeu esta Necessidade."));

        //sem esta checagem, rafazer o envio viraria edição.

        if (studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                demand.getId(), student.getId(), directorId)) {
            throw new EvaluationAlreadySubmittedException(
                    "Este estudante já foi avaliado nesta Necessidade.");
        }

        StudentEvaluation evaluation = new StudentEvaluation();
        evaluation.setDemand(demand);
        evaluation.setStudent(student);
        evaluation.setEvaluatedBy(directorId);
        evaluation.setKnowledge(request.knowledge());
        evaluation.setPunctuality(request.punctuality());
        evaluation.setTeaching(request.teaching());
        evaluation.setCommitment(request.commitment());
        evaluation.setEmpathy(request.empathy());
        evaluation.setComments(request.comments());
        evaluation.setCreatedAt(LocalDateTime.now());

        studentEvaluationRepository.save(evaluation);

        auditService.logCreate(AuditEntityType.EVALUATION, evaluation.getId(), evaluation.getAuditName());

        return feedbackMapper.mapToResponseDTO(evaluation);
    }
}
