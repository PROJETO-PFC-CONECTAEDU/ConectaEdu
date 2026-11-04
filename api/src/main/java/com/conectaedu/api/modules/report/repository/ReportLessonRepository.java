package com.conectaedu.api.modules.report.repository;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.shared.enums.LessonStatus;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReportLessonRepository extends Repository<Lesson, UUID> {

    //uma passada traz tudo que os indicadores de aula precisam.
    List<Lesson> findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
            UUID studentId, Collection<LessonStatus> statuses, LocalDateTime from, LocalDateTime to);
}
