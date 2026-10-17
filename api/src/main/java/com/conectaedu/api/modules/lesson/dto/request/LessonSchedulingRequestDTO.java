package com.conectaedu.api.modules.lesson.dto.request;

import com.conectaedu.api.shared.enums.LessonOrigin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

//Sem campo de duração, um valor digitado que poderia discordar do início e do fim.
//Quando o diretor não marca nada, origin chega nulo e o service assume NEW.

public record LessonSchedulingRequestDTO(
        @NotNull UUID demandId,
        @NotNull LocalDateTime scheduledStart,
        @NotNull LocalDateTime scheduledEnd,
        @NotNull @Min(3) @Max(10) Integer enrolledCount,
        LessonOrigin origin) {}
