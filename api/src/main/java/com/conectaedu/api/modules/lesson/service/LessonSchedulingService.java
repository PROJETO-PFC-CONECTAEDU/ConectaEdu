package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.request.LessonSchedulingRequestDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.enums.LessonOrigin;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.enums.SchoolStatus;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidLessonOriginException;
import com.conectaedu.api.shared.exceptions.InvalidSchoolStatusException;
import com.conectaedu.api.shared.exceptions.InvalidUniversityStatusException;
import com.conectaedu.api.shared.exceptions.LessonOverlapException;
import com.conectaedu.api.shared.exceptions.LessonScheduleTooSoonException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonSchedulingService {

    private final LessonRepository lessonRepository;
    private final DemandRepository demandRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;

    //A ordem das checagens não é estética, as duas primeiras são conta pura e barram a
    //requisição antes de tocar no banco, e a sobreposição fica no fim por ser a consulta mais cara.

    @Transactional
    public LessonResponseDTO schedule(LessonSchedulingRequestDTO request) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();

        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new IllegalArgumentException("O fim da aula precisa ser depois do início.");
        }

        LocalDateTime earliestStart =
                LocalDateTime.now().plusDays(LessonRules.MIN_NOTICE_DAYS);
        if (request.scheduledStart().isBefore(earliestStart)) {
            throw new LessonScheduleTooSoonException(
                    "A aula deve ser agendada com pelo menos 2 dias de antecedência.");
        }

        Demand demand = demandRepository.findById(request.demandId())
                .orElseThrow(() -> new DemandNotFoundException("Demanda não encontrada!"));

        if (demand.getStatus() != DemandStatus.ONGOING) {
            throw new IllegalArgumentException(
                    "A demanda não aceita novas aulas. Situação atual: " + demand.getStatus());
        }

        Student student = demand.getStudent();
        if (student == null) {
            throw new IllegalArgumentException("A demanda ainda não tem estudante aprovado.");
        }

        if (demand.getDirector() == null || !demand.getDirector().getId().equals(directorId)) {
            throw new AccessDeniedException("Apenas o diretor responsável pela demanda pode agendar aulas.");
        }

        validateInternshipLink(student, demand.getSchool());

        LessonOrigin origin = request.origin() == null ? LessonOrigin.NEW : request.origin();
        if (origin == LessonOrigin.MAKEUP
                && !lessonRepository.existsByDemand_IdAndStatus(demand.getId(), LessonStatus.CANCELED)) {
            throw new InvalidLessonOriginException(
                    "Não há aula cancelada nesta demanda para justificar uma reposição.");
        }

        //O fim da nova aula contra, o início da que já existe, e o início da nova contra o fim dela.

        boolean overlaps = lessonRepository
                .existsByStudent_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        student.getId(), LessonRules.BUSY_STATUSES,
                        request.scheduledEnd(), request.scheduledStart());
        if (overlaps) {
            throw new LessonOverlapException("O estudante já tem aula marcada neste horário.");
        }

        Lesson lesson = new Lesson();
        lesson.setDemand(demand);
        lesson.setStudent(student);
        lesson.setSchool(demand.getSchool());
        lesson.setSubject(demand.getSubject());
        lesson.setScheduledStart(request.scheduledStart());
        lesson.setScheduledEnd(request.scheduledEnd());
        lesson.setStatus(LessonStatus.SCHEDULED);
        lesson.setOrigin(origin);
        lesson.setEnrolledCount(request.enrolledCount());
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setCreatedBy(directorId);

        lessonRepository.save(lesson);

        auditService.logCreate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName());

        return lessonMapper.mapToResponseDTO(lesson);
    }

    //O vínculo Escola-Universidade não existe no código, e a aproximação possível é exigir as duas
    // instituições ativas, senão a aula nasce sem valor como estágio.
    // A checagem de verdade entra aqui no dia em que o vínculo existir.

    private void validateInternshipLink(Student student, School school) {
        University university = student.getUniversity();
        if (university == null) {
            throw new InvalidUniversityStatusException("O estudante não tem universidade vinculada.");
        }
        if (university.getStatus() != UniversityStatus.APPROVED || !university.isActive()) {
            throw new InvalidUniversityStatusException(
                    "A universidade do estudante não está ativa para receber horas de estágio.");
        }
        if (school.getStatus() != SchoolStatus.ACTIVE || !school.isActive()) {
            throw new InvalidSchoolStatusException("A escola não está ativa na plataforma.");
        }
    }
}
