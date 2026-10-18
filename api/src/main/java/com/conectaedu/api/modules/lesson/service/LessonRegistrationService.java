package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.request.LessonRegistrationRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.exceptions.InvalidLessonStatusException;
import com.conectaedu.api.shared.exceptions.LessonNotFoundException;
import com.conectaedu.api.shared.exceptions.RegistrationWindowClosedException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonRegistrationService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;

    //O estudante descreve o que deu em aula, quantos apareceram e classifica a turma no
    //Nine Box, é este registro que leva a aula a COMPLETED e a deixa pronta para validacao.

    @Transactional
    public LessonResponseDTO register(UUID lessonId, LessonRegistrationRequestDTO request) {
        UUID studentId = SecurityContextHolder.getAuthenticatedUserId();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Aula não encontrada!"));

        if (!lesson.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("Apenas o estudante da aula pode registrá-la.");
        }

        if (lesson.getStatus() != LessonStatus.CONFIRMED) {
            throw new InvalidLessonStatusException(
                    "A aula está com status %s e não aceita esta operação.".formatted(lesson.getStatus()));
        }

        //A rotina das 48h já move as atrasadas para NOT_COMPLETED.

        LocalDateTime windowClosesAt = lesson.getScheduledEnd().plusHours(LessonRules.REGISTRATION_WINDOW_HOURS);
        if (LocalDateTime.now().isAfter(windowClosesAt)) {
            throw new RegistrationWindowClosedException(
                    "A janela de 48 horas para registrar esta aula já fechou.");
        }

        //A ausência de alunos não invalida o registro.

        if (request.presentCount() == null || request.presentCount() > lesson.getEnrolledCount()) {
            throw new IllegalArgumentException(
                    "A quantidade de presentes não pode passar a de inscritos, que é "
                            + lesson.getEnrolledCount() + ".");
        }

        LessonStatus beforeStatus = lesson.getStatus();

        lesson.setPresentCount(request.presentCount());
        lesson.setContentTaught(request.contentTaught());
        lesson.setNineBoxPerformance(request.nineBoxPerformance());
        lesson.setNineBoxPotential(request.nineBoxPotential());
        lesson.setStatus(LessonStatus.COMPLETED);
        lesson.setRegisteredAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, lesson.getStatus())
                .field("presentes", null, lesson.getPresentCount())
                .field("nine_box", null, request.nineBoxPerformance() + "/" + request.nineBoxPotential());
        auditService.logUpdate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName(), diff);

        return lessonMapper.mapToResponseDTO(lesson);
    }
}
