package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.exceptions.InvalidLessonStatusException;
import com.conectaedu.api.shared.exceptions.LessonNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonDeletionService {

    private final LessonRepository lessonRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    @Transactional
    public void delete(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Aula não encontrada!"));

        tenancyValidator.validateLessonAccess(lesson);

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new InvalidLessonStatusException(
                    "Somente aulas agendadas podem ser removidas. Status atual: " + lesson.getStatus());
        }

        auditService.logDelete(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName());
        lessonRepository.delete(lesson);
    }
}
