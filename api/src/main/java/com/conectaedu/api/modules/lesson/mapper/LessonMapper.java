package com.conectaedu.api.modules.lesson.mapper;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponseDTO mapToResponseDTO(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getDemand().getId(),
                lesson.getSchool().getName(),
                lesson.getSubject(),
                lesson.getScheduledStart(),
                lesson.getScheduledEnd(),
                lesson.getStatus().name(),
                lesson.getOrigin().name(),
                lesson.getEnrolledCount(),
                lesson.getPresentCount(),
                lesson.getConfirmedAt(),
                lesson.getRegisteredAt(),
                lesson.getSignedAt()
        );
    }
}