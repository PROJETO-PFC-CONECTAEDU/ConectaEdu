package com.conectaedu.api.modules.demand.dto.request;

import com.conectaedu.api.shared.enums.DemandStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record DemandUpdateRequestDTO(
        String title,
        String description,
        String subject,
        String gradeLevel,

        @Min(value = 3, message = "Quantidade de alunos deve ser no mínimo 3")
        @Max(value = 10, message = "Quantidade de alunos deve ser no máximo 10")
        Integer pupilAmount,

        @FutureOrPresent(message = "A data da aula não pode ser no passado")
        LocalDateTime classDate,

        String totalHours,
        String room,
        String difficultyLevel,
        DemandStatus status
) {
}
