package com.conectaedu.api.modules.lesson.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponseDTO(
        UUID id,
        UUID demandId,
        String schoolName,
        String subject,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        String status,
        String origin,
        Integer enrolledCount,
        Integer presentCount,
        LocalDateTime confirmedAt,
        LocalDateTime registeredAt,
        LocalDateTime signedAt) {}
