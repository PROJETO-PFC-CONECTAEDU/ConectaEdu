package com.conectaedu.api.modules.feedback.dto.response;

import java.util.List;
import java.util.UUID;

//o que a escola disse do estudante e o que o estudante disse da sala.

public record DemandEvaluationsResponseDTO(
        UUID demandId,
        List<StudentEvaluationResponseDTO> studentEvaluations,
        List<ClassEvaluationResponseDTO> classEvaluations) {}
