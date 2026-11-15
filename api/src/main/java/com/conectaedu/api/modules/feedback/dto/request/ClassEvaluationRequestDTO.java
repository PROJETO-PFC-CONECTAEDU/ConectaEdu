package com.conectaedu.api.modules.feedback.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

//quem avalia a sala é o estudante autenticado.

public record ClassEvaluationRequestDTO(
        @NotNull UUID demandId,
        @NotNull @Min(0) @Max(10) Integer engagement,
        @NotNull @Min(0) @Max(10) Integer progress,
        @NotNull @Min(0) @Max(10) Integer infrastructure,
        @Size(max = 2000) String comments) {}
