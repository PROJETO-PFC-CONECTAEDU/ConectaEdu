package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
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
public class LessonSignatureService {

    private final LessonRepository lessonRepository;
    private final UniversityAdminRepository universityAdminRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;

    //A assinatura é o que transforma a aula em hora de estágio, normalmente assina o diretor da escola
    // passados os 10 dias a rotina escala o caso e a universidade do estudante pode assinar
    //no lugar dele.

    @Transactional
    public LessonResponseDTO sign(UUID lessonId) {
        UUID actorId = SecurityContextHolder.getAuthenticatedUserId();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Aula não encontrada!"));

        if (lesson.getStatus() != LessonStatus.COMPLETED) {
            throw new InvalidLessonStatusException(
                    "A aula está com status %s e não aceita esta operação.".formatted(lesson.getStatus()));
        }

        if (!canSign(lesson, actorId)) {
            throw new AccessDeniedException(
                    "Só o diretor da escola assina esta aula. A universidade assina depois do escalonamento.");
        }

        LessonStatus beforeStatus = lesson.getStatus();

        lesson.setStatus(LessonStatus.VALIDATED);
        lesson.setSignedAt(LocalDateTime.now());
        lesson.setSignedBy(actorId);
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, lesson.getStatus())
                .field("assinada_por", null, actorId);
        auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);

        return lessonMapper.mapToResponseDTO(lesson);
    }

    private boolean canSign(Lesson lesson, UUID actorId) {
        boolean isDemandDirector = lesson.getDemand().getDirector() != null
                && lesson.getDemand().getDirector().getId().equals(actorId);
        if (isDemandDirector) {
            return true;
        }

        //Antes do escalonamento a universidade não tem o que assinar.

        if (lesson.getEscalatedAt() == null) {
            return false;
        }

        UniversityAdmin admin = universityAdminRepository.findById(actorId).orElse(null);
        if (admin == null || admin.getUniversity() == null || lesson.getStudent().getUniversity() == null) {
            return false;
        }
        return admin.getUniversity().getId().equals(lesson.getStudent().getUniversity().getId());
    }
}