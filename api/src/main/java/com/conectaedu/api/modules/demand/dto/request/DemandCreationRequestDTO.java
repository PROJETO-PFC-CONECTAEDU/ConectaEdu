package com.conectaedu.api.modules.demand.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DemandCreationRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String title,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotBlank(message = "Disciplina é obrigatória")
        String subject,

        @NotBlank(message = "Nível de escolaridade é obrigatório")
        String gradeLevel,

        @NotNull(message = "Quantidade de alunos é obrigatória")
        @Min(value = 3, message = "Quantidade de alunos deve ser no mínimo 3")
        @Max(value = 10, message = "Quantidade de alunos deve ser no máximo 10")
        Integer pupilAmount,

        @NotNull(message = "Data da aula é obrigatória")
        @FutureOrPresent(message = "A data da aula não pode ser no passado")
        LocalDateTime classDate,

        @NotBlank(message = "Total de horas é obrigatório")
        String totalHours,

        @NotBlank(message = "Sala é obrigatória")
        String room,

        @NotBlank(message = "Grau de dificuldade é obrigatório")
        String difficultyLevel
) {
}
