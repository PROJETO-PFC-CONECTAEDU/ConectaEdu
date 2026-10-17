package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.response.LessonHoursResponseDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.shared.enums.LessonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonListByService {

    //Esses dois limites cobrem qualquer aula que a plataforma venha a ter
    // e evitam um segundo par de consultas derivadas.

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 1, 1, 0, 0);

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    //Alimenta a tela da agenda.

    @Transactional(readOnly = true)
    public List<LessonResponseDTO> listByStudent(UUID studentId, LocalDateTime from,
                                                 LocalDateTime to, LessonStatus status) {
        LocalDateTime start = from == null ? MIN_DATE : from;
        LocalDateTime end = to == null ? MAX_DATE : to;

        List<Lesson> lessons = status == null
                ? lessonRepository.findByStudent_IdAndScheduledStartBetweenOrderByScheduledStartAsc(
                studentId, start, end)
                : lessonRepository.findByStudent_IdAndStatusAndScheduledStartBetweenOrderByScheduledStartAsc(
                studentId, status, start, end);

        return lessons.stream().map(lessonMapper::mapToResponseDTO).toList();
    }

    //Não existe tabela de horas: o total é a soma das durações das aulas em VALIDATED.

    @Transactional(readOnly = true)
    public LessonHoursResponseDTO totalValidatedHours(UUID studentId) {
        List<Lesson> validated = lessonRepository.findByStudent_IdAndStatus(studentId, LessonStatus.VALIDATED);

        long minutes = validated.stream()
                .mapToLong(lesson -> lesson.getDuration().toMinutes())
                .sum();

        return new LessonHoursResponseDTO(studentId, minutes, validated.size());
    }
}
