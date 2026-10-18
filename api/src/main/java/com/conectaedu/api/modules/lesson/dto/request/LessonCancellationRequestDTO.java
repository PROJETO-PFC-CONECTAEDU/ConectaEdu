package com.conectaedu.api.modules.lesson.dto.request;

import com.conectaedu.api.shared.enums.CancellationReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonCancellationRequestDTO(
        @NotNull CancellationReason reason,
        @Size(max = 500) String notes) {}
