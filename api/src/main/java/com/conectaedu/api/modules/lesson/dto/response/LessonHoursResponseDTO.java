package com.conectaedu.api.modules.lesson.dto.response;

import java.util.UUID;

//O total não fica guardado em tabela, é somado na consulta, a partir das aulas com status VALIDATED.

public record LessonHoursResponseDTO(
        UUID studentId,
        long totalMinutes,
        int validatedLessons) {}