package com.conectaedu.api.modules.feedback.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassEvaluationResponseDTO(
        UUID id,
        UUID demandId,
        String demandTitle,
        UUID studentId,
        String studentName,
        Integer engagement,
        Integer progress,
        Integer infrastructure,
        String comments,
        LocalDateTime createdAt) {}
