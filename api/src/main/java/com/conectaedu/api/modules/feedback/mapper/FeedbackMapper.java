package com.conectaedu.api.modules.feedback.mapper;

import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public StudentEvaluationResponseDTO mapToResponseDTO(StudentEvaluation evaluation) {
        return new StudentEvaluationResponseDTO(
                evaluation.getId(),
                evaluation.getDemand().getId(),
                evaluation.getDemand().getTitle(),
                evaluation.getStudent().getId(),
                evaluation.getStudent().getName(),
                evaluation.getEvaluatedBy(),
                evaluation.getKnowledge(),
                evaluation.getPunctuality(),
                evaluation.getTeaching(),
                evaluation.getCommitment(),
                evaluation.getEmpathy(),
                average(evaluation),
                evaluation.getComments(),
                evaluation.getCreatedAt()
        );
    }

    public ClassEvaluationResponseDTO mapToResponseDTO(ClassEvaluation evaluation) {
        return new ClassEvaluationResponseDTO(
                evaluation.getId(),
                evaluation.getDemand().getId(),
                evaluation.getDemand().getTitle(),
                evaluation.getStudent().getId(),
                evaluation.getStudent().getName(),
                evaluation.getEngagement(),
                evaluation.getProgress(),
                evaluation.getInfrastructure(),
                evaluation.getComments(),
                evaluation.getCreatedAt()
        );
    }

    //a nota final da RN-47 é a média aritmética dos cinco critérios, não é guardada.
    private Double average(StudentEvaluation evaluation) {
        int sum = evaluation.getKnowledge()
                + evaluation.getPunctuality()
                + evaluation.getTeaching()
                + evaluation.getCommitment()
                + evaluation.getEmpathy();
        return sum / 5.0;
    }
}
