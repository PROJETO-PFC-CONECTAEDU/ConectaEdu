package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.exceptions.InvalidLessonStatusException;
import com.conectaedu.api.shared.exceptions.LessonNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonConfirmationService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;

    //Quem confirma é o estudante da aula, e só enquanto a aula está SCHEDULED.

    @Transactional
    public LessonResponseDTO confirm(UUID lessonId) {
        UUID studentId = SecurityContextHolder.getAuthenticatedUserId();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Aula não encontrada!"));

        if (!lesson.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("Apenas o estudante da aula pode confirmá-la.");
        }

        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            throw new InvalidLessonStatusException(
                    "A aula está com status %s e não aceita esta operação.".formatted(lesson.getStatus()));
        }

        LessonStatus beforeStatus = lesson.getStatus();

        lesson.setStatus(LessonStatus.CONFIRMED);
        lesson.setConfirmedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);

        AuditDiff diff = AuditDiff.create().field("status", beforeStatus, lesson.getStatus());
        auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);

        return lessonMapper.mapToResponseDTO(lesson);
    }
}
