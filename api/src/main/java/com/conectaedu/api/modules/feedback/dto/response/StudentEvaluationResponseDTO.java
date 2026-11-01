package com.conectaedu.api.modules.feedback.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentEvaluationResponseDTO(
        UUID id,
        UUID demandId,
        String demandTitle,
        UUID studentId,
        String studentName,
        UUID evaluatedBy,
        Integer knowledge,
        Integer punctuality,
        Integer teaching,
        Integer commitment,
        Integer empathy,
        Double average,
        String comments,
        LocalDateTime createdAt) {}
