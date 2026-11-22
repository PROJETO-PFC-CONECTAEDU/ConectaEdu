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
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
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
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonSchedulingService {

    private final LessonRepository lessonRepository;
    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final LessonMapper lessonMapper;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    /**
     * Agenda uma nova aula para uma demanda.
     * A ordem das checagens barram a requisição antes de tocar no banco sempre que possível,
     * e a sobreposição fica por fim por ser a consulta mais custosa.
     */
    @Transactional
    public LessonResponseDTO schedule(LessonSchedulingRequestDTO request) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();
        LocalDateTime now = LocalDateTime.now();

        validateTimeRange(request);
        validateNoticePeriod(request, now);

        // Bloqueio pessimista para evitar condições de corrida em agendamentos simultâneos
        Demand demand = demandRepository.findByIdWithLock(request.demandId())
                .orElseThrow(() -> new DemandNotFoundException("Demanda não encontrada!"));

        tenancyValidator.validateDemandAccess(demand);
        validateDemandStatus(demand);

        Student student = demand.getStudent();
        if (student != null) {
            // Também bloqueia o estudante para evitar sobreposição em demandas diferentes
            studentRepository.findByIdWithLock(student.getId());
            validateInternshipLink(student, demand.getSchool());
        }

        LessonOrigin origin = request.origin() == null ? LessonOrigin.NEW : request.origin();
        validateLessonOrigin(demand, origin);

        validateOverlaps(request, demand, student);

        Lesson lesson = createLesson(request, demand, student, origin, directorId, now);
        lessonRepository.save(lesson);

        auditService.logCreate(AuditEntityType.LESSON, lesson.getId(), lesson.getAuditName());

        return lessonMapper.mapToResponseDTO(lesson);
    }

    private void validateTimeRange(LessonSchedulingRequestDTO request) {
        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new IllegalArgumentException("O fim da aula precisa ser depois do início.");
        }
    }

    private void validateNoticePeriod(LessonSchedulingRequestDTO request, LocalDateTime now) {
        // Regra de antecedência mínima: 2 dias. 
        // Usamos LocalDate para permitir agendamentos em qualquer horário no dia D+2.
        if (request.scheduledStart().toLocalDate().isBefore(now.toLocalDate().plusDays(LessonRules.MIN_NOTICE_DAYS))) {
            throw new LessonScheduleTooSoonException(
                    "A aula deve ser agendada com pelo menos 2 dias de antecedência.");
        }
    }

    private void validateDemandStatus(Demand demand) {
        if (demand.getStatus() != DemandStatus.ONGOING &&
            demand.getStatus() != DemandStatus.DRAFT &&
            demand.getStatus() != DemandStatus.WAITING) {
            throw new IllegalArgumentException(
                    "A demanda não aceita novas aulas. Situação atual: " + demand.getStatus());
        }
    }

    private void validateLessonOrigin(Demand demand, LessonOrigin origin) {
        if (origin == LessonOrigin.MAKEUP
                && !lessonRepository.existsByDemand_IdAndStatus(demand.getId(), LessonStatus.CANCELED)) {
            throw new InvalidLessonOriginException(
                    "Não há aula cancelada nesta demanda para justificar uma reposição.");
        }
    }

    private void validateOverlaps(LessonSchedulingRequestDTO request, Demand demand, Student student) {
        boolean demandOverlaps = lessonRepository
                .existsByDemand_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
                        demand.getId(), LessonRules.BUSY_STATUSES,
                        request.scheduledEnd(), request.scheduledStart());
        if (demandOverlaps) {
            throw new LessonOverlapException("Já existe uma aula agendada para esta demanda neste horário.");
        }

        if (student != null) {
            boolean studentOverlaps = lessonRepository
                    .existsByStudent_IdAndStatusInAndScheduledStartLessThanAndScheduledEndGreaterThan(
                            student.getId(), LessonRules.BUSY_STATUSES,
                            request.scheduledEnd(), request.scheduledStart());
            if (studentOverlaps) {
                throw new LessonOverlapException("O estudante já tem aula marcada neste horário.");
            }
        }
    }

    private Lesson createLesson(LessonSchedulingRequestDTO request, Demand demand, Student student, 
                                LessonOrigin origin, UUID directorId, LocalDateTime now) {
        Lesson lesson = new Lesson();
        lesson.setDemand(demand);
        lesson.setStudent(student);
        lesson.setSchool(demand.getSchool());
        lesson.setSubject(demand.getSubject());
        // Normaliza para minutos para evitar problemas com segundos/nanos em comparações futuras
        lesson.setScheduledStart(request.scheduledStart().withSecond(0).withNano(0));
        lesson.setScheduledEnd(request.scheduledEnd().withSecond(0).withNano(0));
        lesson.setStatus(LessonStatus.SCHEDULED);
        lesson.setOrigin(origin);
        lesson.setEnrolledCount(request.enrolledCount());
        lesson.setCreatedAt(now);
        lesson.setUpdatedAt(now);
        lesson.setCreatedBy(directorId);
        return lesson;
    }

    // O vínculo Escola-Universidade não existe no código, e a aproximação possível é exigir as duas
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
