package com.conectaedu.api.modules.lesson.service;

import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.dto.response.LessonHoursResponseDTO;
import com.conectaedu.api.modules.lesson.dto.response.LessonResponseDTO;
import com.conectaedu.api.modules.lesson.mapper.LessonMapper;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final StudentRepository studentRepository;
    private final UniversityAdminRepository universityAdminRepository;
    private final SchoolDirectorRepository schoolDirectorRepository;

    //Alimenta a tela da agenda.

    @Transactional(readOnly = true)
    public List<LessonResponseDTO> listByStudent(UUID studentId, LocalDateTime from,
                                                 LocalDateTime to, LessonStatus status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        checkAccess(student);

        LocalDateTime start = from == null ? MIN_DATE : from;
        LocalDateTime end = to == null ? MAX_DATE : to;

        List<Lesson> lessons = status == null
                ? lessonRepository.findByStudent_IdAndScheduledStartBetweenOrderByScheduledStartAsc(
                studentId, start, end)
                : lessonRepository.findByStudent_IdAndStatusAndScheduledStartBetweenOrderByScheduledStartAsc(
                studentId, status, start, end);

        if (hasRole(UserRole.SCHOOL_DIRECTOR) && !hasRole(UserRole.PLATFORM_ADMIN)) {
            UUID actorId = SecurityContextHolder.getAuthenticatedUserId();
            SchoolDirector director = schoolDirectorRepository.findById(actorId).orElse(null);
            if (director != null && director.getSchool() != null) {
                UUID schoolId = director.getSchool().getId();
                return lessons.stream()
                        .filter(l -> l.getSchool().getId().equals(schoolId))
                        .map(lessonMapper::mapToResponseDTO)
                        .toList();
            }
        }

        return lessons.stream().map(lessonMapper::mapToResponseDTO).toList();
    }

    //Não existe tabela de horas: o total é a soma das durações das aulas em VALIDATED.

    @Transactional(readOnly = true)
    public LessonHoursResponseDTO totalValidatedHours(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        checkAccess(student);

        List<Lesson> validated = lessonRepository.findByStudent_IdAndStatus(studentId, LessonStatus.VALIDATED);

        long minutes = validated.stream()
                .mapToLong(lesson -> lesson.getDuration().toMinutes())
                .sum();

        return new LessonHoursResponseDTO(studentId, minutes, validated.size());
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDTO> listBySchool(UUID schoolId) {
        checkSchoolAccess(schoolId);

        List<Lesson> lessons = lessonRepository.findBySchool_IdOrderByScheduledStartAsc(schoolId);

        return lessons.stream().map(lessonMapper::mapToResponseDTO).toList();
    }

    private void checkSchoolAccess(UUID schoolId) {
        UUID actorId = SecurityContextHolder.getAuthenticatedUserId();

        if (hasRole(UserRole.PLATFORM_ADMIN)) {
            return;
        }

        SchoolDirector director = schoolDirectorRepository.findById(actorId).orElse(null);
        boolean sameSchool = director != null
                && director.getSchool() != null
                && director.getSchool().getId().equals(schoolId);

        if (!sameSchool) {
            throw new AccessDeniedException("Você não tem acesso aos dados desta escola.");
        }
    }

    private void checkAccess(Student student) {
        UUID actorId = SecurityContextHolder.getAuthenticatedUserId();

        if (student.getId().equals(actorId)) {
            return;
        }

        if (hasRole(UserRole.PLATFORM_ADMIN) || hasRole(UserRole.SCHOOL_DIRECTOR)) {
            return;
        }

        UniversityAdmin admin = universityAdminRepository.findById(actorId).orElse(null);
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
