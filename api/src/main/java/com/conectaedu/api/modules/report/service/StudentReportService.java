package com.conectaedu.api.modules.report.service;

import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.interfaces.IFeedbackFacade;
import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.interfaces.IInternshipFacade;
import com.conectaedu.api.modules.internship.support.InternshipRules;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.report.dto.response.ClassProgressPointDTO;
import com.conectaedu.api.modules.report.dto.response.StudentReportResponseDTO;
import com.conectaedu.api.modules.report.repository.ReportLessonRepository;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentReportService {

    //os mesmos limites do EvaluationListByService, para os dois módulos responderem igual a um período aberto.
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 1, 1, 0, 0);
    private static final Set<LessonStatus> REPORT_STATUSES = EnumSet.of(
            LessonStatus.COMPLETED,
            LessonStatus.VALIDATED,
            LessonStatus.CANCELED,
            LessonStatus.NOT_COMPLETED);

    private final StudentRepository studentRepository;
    private final UniversityAdminRepository universityAdminRepository;
    private final SchoolRepository schoolRepository;
    private final ReportLessonRepository reportLessonRepository;
    private final IInternshipFacade internshipFacade;
    private final IFeedbackFacade feedbackFacade;

    @Transactional(readOnly = true)
    public StudentReportResponseDTO forStudent(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        Student student = findAndCheck(studentId, from, to, schoolId);

        LocalDateTime start = startOf(from);
        LocalDateTime end = endOf(to);

        List<Lesson> lessons = lessonsOf(studentId, start, end, schoolId);

        //carga e saldo nunca são filtrados, a obrigação é da universidade e vale para o estágio inteiro.
        InternshipHoursResponseDTO hours = internshipFacade.getHoursByStudent(studentId);

        long validatedMinutes = minutesOf(lessons, LessonStatus.VALIDATED);
        long pendingMinutes = minutesOf(lessons, LessonStatus.COMPLETED);
        int performed = (int) lessons.stream().filter(this::isPerformed).count();
        int canceled = (int) countOf(lessons, LessonStatus.CANCELED);
        int notCompleted = (int) countOf(lessons, LessonStatus.NOT_COMPLETED);

        //soma em minutos e converte uma vez por número.
        BigDecimal validatedHours = InternshipRules.toHours(validatedMinutes);
        BigDecimal pendingHours = InternshipRules.toHours(pendingMinutes);

        List<StudentEvaluationResponseDTO> evaluations =
                feedbackFacade.getEvaluationsByStudent(studentId, null, null).stream()
                        .filter(e -> e.finishedAt() != null)
                        .filter(e -> !e.finishedAt().isBefore(start) && !e.finishedAt().isAfter(end))
                        .filter(e -> schoolId == null || schoolId.equals(e.schoolId()))
                        .toList();

        //lista vazia devolve null, pois zero é nota.
        Double averageReceived = evaluations.isEmpty() ? null
                : evaluations.stream()
                        .mapToDouble(StudentEvaluationResponseDTO::average)
                        .average().orElse(0d);

        return new StudentReportResponseDTO(
                studentId,
                student.getName(),
                from,
                to,
                schoolId,
                hours.totalHours(),
                hours.requiredHours(),
                hours.remainingHours(),
                hours.requirementMetAt(),
                performed,
                validatedHours,
                pendingHours,
                canceled,
                notCompleted,
                averageReceived,
                evaluations,
                progressOf(lessons));
    }

    //a série sozinha, para o gráfico trocar de período sem recarregar o relatório todo.
    @Transactional(readOnly = true)
    public List<ClassProgressPointDTO> classProgress(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        findAndCheck(studentId, from, to, schoolId);

        return progressOf(lessonsOf(studentId, startOf(from), endOf(to), schoolId));
    }

    private Student findAndCheck(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        checkAccess(student);

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de término.");
        }

        if (schoolId != null && !schoolRepository.existsById(schoolId)) {
            throw new SchoolNotFoundException("Escola não encontrada!");
        }

        return student;
    }

    private LocalDateTime startOf(LocalDate from) {
        return from == null ? MIN_DATE : from.atStartOfDay();
    }

    private LocalDateTime endOf(LocalDate to) {
        return to == null ? MAX_DATE : to.atTime(LocalTime.MAX);
    }

    private List<Lesson> lessonsOf(UUID studentId, LocalDateTime start, LocalDateTime end, UUID schoolId) {
        return reportLessonRepository
                .findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                        studentId, REPORT_STATUSES, start, end)
                .stream()
                .filter(l -> schoolId == null || schoolId.equals(l.getSchool().getId()))
                .toList();
    }

    private List<ClassProgressPointDTO> progressOf(List<Lesson> lessons) {
        return lessons.stream()
                .filter(this::isPerformed)
                .filter(l -> l.getNineBoxPerformance() != null && l.getNineBoxPotential() != null)
                .map(l -> new ClassProgressPointDTO(
                        l.getId(), l.getScheduledEnd(),
                        l.getNineBoxPerformance(), l.getNineBoxPotential()))
                .toList();
    }

    //atendimentos realizados e série do Nine Box.
    private boolean isPerformed(Lesson lesson) {
        return lesson.getStatus() == LessonStatus.COMPLETED || lesson.getStatus() == LessonStatus.VALIDATED;
    }

    private long minutesOf(List<Lesson> lessons, LessonStatus status) {
        return lessons.stream()
                .filter(l -> l.getStatus() == status)
                .mapToLong(l -> l.getDuration().toMinutes())
                .sum();
    }

    private long countOf(List<Lesson> lessons, LessonStatus status) {
        return lessons.stream().filter(l -> l.getStatus() == status).count();
    }


    private void checkAccess(Student student) {
        UUID actorId = SecurityContextHolder.getAuthenticatedUserId();

        if (student.getId().equals(actorId)) {
            return;
        }

        if (hasRole(UserRole.PLATFORM_ADMIN)) {
            return;
        }

        UniversityAdmin admin =
                universityAdminRepository.findById(actorId).orElse(null);

        boolean sameUniversity = admin != null
                && admin.getUniversity() != null
                && student.getUniversity() != null
                && admin.getUniversity().getId().equals(student.getUniversity().getId());

        if (!sameUniversity) {
            throw new AccessDeniedException("Você não tem acesso aos dados deste estudante.");
        }
    }

    private boolean hasRole(UserRole role) {
        AuthenticatedUser user = SecurityContextHolder.getAuthenticatedUser();
        return user != null && user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }
}
