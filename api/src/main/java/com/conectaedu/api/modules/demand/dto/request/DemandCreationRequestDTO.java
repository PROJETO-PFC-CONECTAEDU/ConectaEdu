package com.conectaedu.api.modules.demand.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;

public record DemandCreationRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String title,

        @NotBlank(message = "Descrição é obrigatória")
        String description,

        @NotBlank(message = "Disciplina é obrigatória")
        String subject,

        //Torna-se opcional
        String gradeLevel,

        //Deixa de ter minimo e máximo e passa a ter validação na service
        @NotNull(message = "Quantidade de alunos é obrigatória")
        Integer pupilAmount,

        @NotNull(message = "Data da aula é obrigatória")
        @FutureOrPresent(message = "A data da aula não pode ser no passado")
        LocalDateTime classDate,

        @NotBlank(message = "Total de horas é obrigatório")
        String totalHours,

        //Torna-se opcional
        String room,

        @NotBlank(message = "Grau de dificuldade é obrigatório")
        String difficultyLevel
) {
}
