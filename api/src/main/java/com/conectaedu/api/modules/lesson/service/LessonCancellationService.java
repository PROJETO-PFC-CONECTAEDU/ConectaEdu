package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.request.LessonCancellationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.event.LessonCancelledEvent;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.CancellationReason;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.exceptions.InvalidLessonStatusException;
import com.conectaedu.api.shared.exceptions.LessonNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonCancellationService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    //Aula cancelada não é apagada, ela fica no histórico da demanda em CANCELED, e é daí
    //que sai a permissão para abrir uma reposição.

    @Transactional
    public LessonResponseDTO cancel(UUID lessonId, LessonCancellationRequestDTO request) {
        UUID actorId = SecurityContextHolder.getAuthenticatedUserId();

        if (request.reason() == CancellationReason.OTHER
                && (request.notes() == null || request.notes().isBlank())) {
            throw new IllegalArgumentException("Cancelamento com o motivo OTHER exige uma justificativa.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Aula não encontrada!"));

        boolean isStudent = lesson.getStudent().getId().equals(actorId);
        boolean isDirector = lesson.getDemand().getDirector() != null
                && lesson.getDemand().getDirector().getId().equals(actorId);
        if (!isStudent && !isDirector) {
            throw new AccessDeniedException("Só o estudante da aula ou o diretor da escola podem cancelá-la.");
        }

        if (lesson.getStatus() != LessonStatus.SCHEDULED && lesson.getStatus() != LessonStatus.CONFIRMED) {
            throw new InvalidLessonStatusException(
                    "A aula está com status %s e não aceita esta operação.".formatted(lesson.getStatus()));
        }

        LessonStatus beforeStatus = lesson.getStatus();

        lesson.setStatus(LessonStatus.CANCELED);
        lesson.setCancellationReason(request.reason());
        lesson.setCancellationNotes(request.notes());
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, lesson.getStatus())
                .field("motivo_cancelamento", null, lesson.getCancellationReason());
        auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);

        //Quem deve mandar o e-mail é o modulo notify, que "escuta" este evento.

        eventPublisher.publishEvent(new LessonCancelledEvent(
                lesson.getId(),
                lesson.getStudent().getId(),
                lesson.getDemand().getDirector() == null ? null : lesson.getDemand().getDirector().getId(),
                request.reason(),
                false));

        return lessonMapper.mapToResponseDTO(lesson);
    }
}

