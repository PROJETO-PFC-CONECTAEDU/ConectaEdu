package com.conectaedu.api.modules.feedback.service;

import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.DemandEvaluationsResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.mapper.FeedbackMapper;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvaluationListByService {

    //esses dois limites cobrem qualquer avaliação que a plataforma venha a ter.
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 1, 1, 0, 0);

    private final StudentRepository studentRepository;
    private final DemandRepository demandRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final ClassEvaluationRepository classEvaluationRepository;
    private final FeedbackMapper feedbackMapper;
    private final TenancyValidator tenancyValidator;

    //histórico de avaliações recebidas pelo estudante RN-52. o estudante só vê o próprio.
    @Transactional(readOnly = true)
    public List<StudentEvaluationResponseDTO> listByStudent(UUID studentId, LocalDateTime from, LocalDateTime to) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de término.");
        }

        tenancyValidator.validateStudentAccess(student);

        LocalDateTime start = from == null ? MIN_DATE : from;
        LocalDateTime end = to == null ? MAX_DATE : to;

        return studentEvaluationRepository
                .findByStudent_IdAndCreatedAtBetweenOrderByCreatedAtDesc(studentId, start, end)
                .stream().map(feedbackMapper::mapToResponseDTO).toList();
    }

    //os dois lados da mesma necessidade.
    @Transactional(readOnly = true)
    public DemandEvaluationsResponseDTO listByDemand(UUID demandId) {
        com.conectaedu.api.modules.demand.domain.Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new DemandNotFoundException("Necessidade não encontrada!"));

        tenancyValidator.validateDemandAccess(demand);

        List<StudentEvaluationResponseDTO> studentEvaluations = studentEvaluationRepository
                .findByDemand_IdOrderByCreatedAtDesc(demandId)
                .stream().map(feedbackMapper::mapToResponseDTO).toList();

        List<ClassEvaluationResponseDTO> classEvaluations = classEvaluationRepository
                .findByDemand_IdOrderByCreatedAtDesc(demandId)
                .stream().map(feedbackMapper::mapToResponseDTO).toList();

        return new DemandEvaluationsResponseDTO(demandId, studentEvaluations, classEvaluations);
    }

    @Transactional(readOnly = true)
    public List<ClassEvaluationResponseDTO> listByDirector(UUID directorId) {
        tenancyValidator.validateUserAccess(directorId);

        return classEvaluationRepository
                .findByDemand_Director_IdOrderByCreatedAtDesc(directorId)
                .stream().map(feedbackMapper::mapToResponseDTO).toList();
    }
}
