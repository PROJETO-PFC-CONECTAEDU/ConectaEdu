package com.conectaedu.api.modules.report.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassProgressPointDTO(
        UUID lessonId,
        LocalDateTime lessonDate,
        Integer performance,
        Integer potential) {}
