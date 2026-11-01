package com.conectaedu.api.modules.feedback.facade;

import com.conectaedu.api.modules.feedback.dto.request.ClassEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.request.StudentEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.DemandEvaluationsResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.interfaces.IFeedbackFacade;
import com.conectaedu.api.modules.feedback.service.ClassEvaluationService;
import com.conectaedu.api.modules.feedback.service.EvaluationListByService;
import com.conectaedu.api.modules.feedback.service.StudentEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeedbackFacadeImpl implements IFeedbackFacade {

    private final StudentEvaluationService studentEvaluationService;
    private final ClassEvaluationService classEvaluationService;
    private final EvaluationListByService evaluationListByService;

    @Override
    public StudentEvaluationResponseDTO evaluateStudent(StudentEvaluationRequestDTO request) {
        return studentEvaluationService.evaluate(request);
    }

    @Override
    public ClassEvaluationResponseDTO evaluateClass(ClassEvaluationRequestDTO request) {
        return classEvaluationService.evaluate(request);
    }

    @Override
    public List<StudentEvaluationResponseDTO> getEvaluationsByStudent(UUID studentId, LocalDateTime from,
                                                                      LocalDateTime to) {
        return evaluationListByService.listByStudent(studentId, from, to);
    }

    @Override
    public DemandEvaluationsResponseDTO getEvaluationsByDemand(UUID demandId) {
        return evaluationListByService.listByDemand(demandId);
    }
}
