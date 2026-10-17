package com.conectaedu.api.modules.lesson.facade;

import com.conectaedu.api.modules.lesson.dto.request.LessonCancellationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonRegistrationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.request.LessonSchedulingRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonHoursResponseDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.interfaces.ILessonFacade;
import com.conectaedu.api.modules.lesson.service.*;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LessonFacadeImpl implements ILessonFacade {

    private final LessonSchedulingService lessonSchedulingService;
    private final LessonConfirmationService lessonConfirmationService;
    private final LessonCancellationService lessonCancellationService;
    private final LessonRegistrationService lessonRegistrationService;
    private final LessonSignatureService lessonSignatureService;
    private final LessonListByService lessonListByService;

    @Override
    public LessonResponseDTO scheduleLesson(LessonSchedulingRequestDTO request) {
        return lessonSchedulingService.schedule(request);
    }

    @Override
    public LessonResponseDTO confirmLesson(UUID lessonId) {
        return lessonConfirmationService.confirm(lessonId);
    }

    @Override
    public LessonResponseDTO cancelLesson(UUID lessonId, LessonCancellationRequestDTO request) {
        return lessonCancellationService.cancel(lessonId, request);
    }

    @Override
    public LessonResponseDTO registerLesson(UUID lessonId, LessonRegistrationRequestDTO request) {
        return lessonRegistrationService.register(lessonId, request);
    }

    @Override
    public LessonResponseDTO signLesson(UUID lessonId) {
        return lessonSignatureService.sign(lessonId);
    }

    @Override
    public List<LessonResponseDTO> getLessonsByStudent(UUID studentId, LocalDateTime from,
                                                       LocalDateTime to, LessonStatus status) {
        return lessonListByService.listByStudent(studentId, from, to, status);
    }

    @Override
    public LessonHoursResponseDTO getValidatedHoursByStudent(UUID studentId) {
        return lessonListByService.totalValidatedHours(studentId);
    }
}

