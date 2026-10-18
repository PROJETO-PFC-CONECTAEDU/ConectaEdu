package com.conectaedu.api.modules.lesson.event;

import com.conectaedu.api.shared.enums.CancellationReason;
import java.util.UUID;

public record LessonCancelledEvent(UUID lessonId, UUID studentId, UUID directorId, CancellationReason reason, boolean bySystem) {}
