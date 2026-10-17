package com.conectaedu.api.modules.lesson.interfaces;

import com.conectaedu.api.modules.lesson.dto.request.LessonCancellationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonRegistrationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonSchedulingRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonHoursResponseDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.shared.enums.LessonStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ILessonFacade {

    LessonResponseDTO scheduleLesson(LessonSchedulingRequestDTO request);

    LessonResponseDTO confirmLesson(UUID lessonId);

    LessonResponseDTO cancelLesson(UUID lessonId, LessonCancellationRequestDTO request);

    LessonResponseDTO registerLesson(UUID lessonId, LessonRegistrationRequestDTO request);

    LessonResponseDTO signLesson(UUID lessonId);

    List<LessonResponseDTO> getLessonsByStudent(UUID studentId, LocalDateTime from,
                                                LocalDateTime to, LessonStatus status);

    LessonHoursResponseDTO getValidatedHoursByStudent(UUID studentId);
}
