package com.conectaedu.api.modules.internship.service;

import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.dto.response.UniversityInternshipResponseDTO;
import com.conectaedu.api.modules.internship.repository.InternshipLessonRepository;
import com.conectaedu.api.modules.internship.repository.InternshipStudentRepository;
import com.conectaedu.api.modules.internship.support.InternshipRules;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternshipHoursService {

    //Os dois primeiros são do próprio módulo.
    //O terceiro é o repositório do university, usado como está, um findById já existe lá.

    private final InternshipLessonRepository internshipLessonRepository;
    private final InternshipStudentRepository internshipStudentRepository;
    private final UniversityRepository universityRepository;
    private final UniversityAdminRepository universityAdminRepository;

    @Transactional(readOnly = true)
    public InternshipHoursResponseDTO forStudent(UUID studentId) {
        Student student = internshipStudentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        checkAccess(student);

        return build(student, requiredHoursOf(student.getUniversity()));
    }

    //A carga é lida uma vez só e vale para a lista inteira, porque é da universidade.

    @Transactional(readOnly = true)
    public UniversityInternshipResponseDTO forUniversity(UUID universityId) {
        checkUniversityAccess(universityId);

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        int requiredHours = requiredHoursOf(university);

        List<InternshipHoursResponseDTO> students =
                internshipStudentRepository.findByUniversity_IdOrderByNameAsc(universityId).stream()
                        .map(student -> build(student, requiredHours))
                        .toList();

        return new UniversityInternshipResponseDTO(universityId, requiredHours, students);
    }

    //Se a universidade não definiu a carga dela, vale o padrão.

    private int requiredHoursOf(University university) {
        if (university == null || university.getRequiredInternshipHours() == null) {
            return InternshipRules.DEFAULT_REQUIRED_HOURS;
        }
        return university.getRequiredInternshipHours();
    }

    private InternshipHoursResponseDTO build(Student student, int requiredHours) {
        UUID studentId = student.getId();
        long requiredMinutes = (long) requiredHours * 60;

        //As aulas chegam na ordem da assinatura, que é a ordem em que viraram hora.

        List<Lesson> validated = internshipLessonRepository
                .findByStudent_IdAndStatusOrderBySignedAtAsc(studentId, LessonStatus.VALIDATED);

        long totalMinutes = 0;
        LocalDate requirementMetAt = null;

        for (Lesson lesson : validated) {
            totalMinutes += lesson.getDuration().toMinutes();

            //A primeira aula a cruzar a carga carimba a data, as seguintes não mexem
            //nela, e as horas continuam somando.

            if (requirementMetAt == null && totalMinutes >= requiredMinutes) {
                requirementMetAt = lesson.getSignedAt().toLocalDate();
            }
        }

        int pendingLessons = (int) internshipLessonRepository
                .countByStudent_IdAndStatus(studentId, LessonStatus.COMPLETED);
        int canceledLessons = (int) internshipLessonRepository
                .countByStudent_IdAndStatus(studentId, LessonStatus.CANCELED);

        return new InternshipHoursResponseDTO(
                studentId,
                student.getName(),
                InternshipRules.toHours(totalMinutes),
                validated.size(),
                pendingLessons,
                canceledLessons,
                requiredHours,
                InternshipRules.toHours(Math.max(0, requiredMinutes - totalMinutes)),
                requirementMetAt);
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

    private void checkUniversityAccess(UUID universityId) {
        if (hasRole(UserRole.PLATFORM_ADMIN)) {
            return;
        }

        AuthenticatedUser user = SecurityContextHolder.getAuthenticatedUser();
        if (user != null && hasRole(UserRole.UNIVERSITY_ADMIN)) {
            if (universityId.equals(user.getUniversityId())) {
                return;
            }
        }

        throw new AccessDeniedException("Você não tem acesso aos dados desta universidade.");
    }

    private boolean hasRole(UserRole role) {
        AuthenticatedUser user = SecurityContextHolder.getAuthenticatedUser();
        return user != null && user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }
}

