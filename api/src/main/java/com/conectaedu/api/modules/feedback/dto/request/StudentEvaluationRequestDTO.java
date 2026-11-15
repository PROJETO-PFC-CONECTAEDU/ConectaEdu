package com.conectaedu.api.modules.feedback.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

//O studentId vem no corpo porque a necessidade pode ter passado por mais de um estudante.
public record StudentEvaluationRequestDTO(
        @NotNull UUID demandId,
        @NotNull UUID studentId,
        @NotNull @Min(0) @Max(10) Integer knowledge,
        @NotNull @Min(0) @Max(10) Integer punctuality,
        @NotNull @Min(0) @Max(10) Integer teaching,
        @NotNull @Min(0) @Max(10) Integer commitment,
        @NotNull @Min(0) @Max(10) Integer empathy,
        @Size(max = 2000) String comments) {}
