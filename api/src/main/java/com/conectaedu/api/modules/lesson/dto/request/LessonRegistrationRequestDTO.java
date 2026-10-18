package com.conectaedu.api.modules.lesson.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonRegistrationRequestDTO(
        @NotNull @Min(0) Integer presentCount,
        @NotBlank @Size(max = 2000) String contentTaught,
        @NotNull @Min(1) @Max(3) Integer nineBoxPerformance,
        @NotNull @Min(1) @Max(3) Integer nineBoxPotential) {}
