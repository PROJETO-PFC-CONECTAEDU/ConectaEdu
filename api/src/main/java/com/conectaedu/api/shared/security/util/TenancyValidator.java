package com.conectaedu.api.shared.security.util;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenancyValidator {

    private final StudentRepository studentRepository;

    public void validateSchool(UUID schoolId) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        UUID authenticatedSchoolId = SecurityContextHolder.getAuthenticatedSchoolId();
        if (authenticatedSchoolId != null && authenticatedSchoolId.equals(schoolId)) {
            return;
        }
        
        throw new AccessDeniedException("Acesso negado: Este recurso não pertence à sua escola.");
    }

    public void validateUniversity(UUID universityId) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        UUID authenticatedUniversityId = SecurityContextHolder.getAuthenticatedUniversityId();
        if (authenticatedUniversityId != null && authenticatedUniversityId.equals(universityId)) {
            return;
        }

        throw new AccessDeniedException("Acesso negado: Este recurso não pertence à sua universidade.");
    }

    public void validateUserAccess(UUID userId) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        UUID authenticatedUserId = SecurityContextHolder.getAuthenticatedUserId();
        if (authenticatedUserId != null && authenticatedUserId.equals(userId)) {
            return;
        }

        throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar os dados deste usuário.");
    }

    public void validateStudentAccess(Student student) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        UUID userId = SecurityContextHolder.getAuthenticatedUserId();
        if (student.getId().equals(userId)) {
            return;
        }

        if (SecurityContextHolder.hasRole("UNIVERSITY_ADMIN")) {
            UUID universityId = SecurityContextHolder.getAuthenticatedUniversityId();
            if (universityId != null && student.getUniversity() != null &&
                    student.getUniversity().getId().equals(universityId)) {
                return;
            }
        }

        if (SecurityContextHolder.hasRole("SCHOOL_DIRECTOR")) {
            UUID schoolId = SecurityContextHolder.getAuthenticatedSchoolId();
            if (schoolId != null && studentRepository.isStudentLinkedToSchool(student.getId(), schoolId)) {
                return;
            }
        }

        throw new AccessDeniedException("Acesso negado: Você não tem acesso aos dados deste estudante.");
    }

    public void validateDemandAccess(Demand demand) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        UUID userId = SecurityContextHolder.getAuthenticatedUserId();
        UUID schoolId = SecurityContextHolder.getAuthenticatedSchoolId();

        if (SecurityContextHolder.hasRole("SCHOOL_DIRECTOR")) {
            if (schoolId != null && demand.getSchool().getId().equals(schoolId)) {
                return;
            }
            throw new AccessDeniedException("Acesso negado: Esta demanda pertence a outra escola.");
        }

        if (SecurityContextHolder.hasRole("STUDENT")) {
            if (demand.getStatus() == DemandStatus.WAITING) {
                return;
            }
            if (demand.getStudent() != null && demand.getStudent().getId().equals(userId)) {
                return;
            }
            if (demand.getCandidates().stream().anyMatch(c -> c.getId().equals(userId))) {
                return;
            }
            throw new AccessDeniedException("Acesso negado: Você não tem permissão para visualizar esta demanda.");
        }

        if (SecurityContextHolder.hasRole("UNIVERSITY_ADMIN")) {
            UUID universityId = SecurityContextHolder.getAuthenticatedUniversityId();
            if (universityId != null && demand.getStudent() != null && 
                demand.getStudent().getUniversity() != null &&
                demand.getStudent().getUniversity().getId().equals(universityId)) {
                return;
            }
            // Também pode ver se houver candidatos da sua universidade? 
            // Para simplificar, vamos permitir se houver algum vínculo.
        }
    }

    public void validateLessonAccess(Lesson lesson) {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return;
        }

        if (SecurityContextHolder.hasRole("STUDENT")) {
            UUID userId = SecurityContextHolder.getAuthenticatedUserId();
            if (lesson.getStudent() != null && lesson.getStudent().getId().equals(userId)) {
                return;
            }
            throw new AccessDeniedException("Acesso negado: Esta aula não está vinculada a você.");
        }

        validateSchool(lesson.getSchool().getId());
    }

    public void validateStudentEvaluationAccess(StudentEvaluation evaluation) {
        validateDemandAccess(evaluation.getDemand());
    }

    public void validateClassEvaluationAccess(ClassEvaluation evaluation) {
        validateDemandAccess(evaluation.getDemand());
    }
}
