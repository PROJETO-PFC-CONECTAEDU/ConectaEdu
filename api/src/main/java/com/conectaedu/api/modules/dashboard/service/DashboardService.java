package com.conectaedu.api.modules.dashboard.service;

import com.conectaedu.api.modules.dashboard.dto.DashboardFilterDTO;
import com.conectaedu.api.modules.dashboard.dto.DashboardStatsDTO;
import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.internship.interfaces.IInternshipFacade;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.enums.StudentStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SchoolRepository schoolRepository;
    private final UniversityRepository universityRepository;
    private final StudentRepository studentRepository;
    private final DemandRepository demandRepository;
    private final LessonRepository lessonRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final ClassEvaluationRepository classEvaluationRepository;
    private final IInternshipFacade internshipFacade;
    private final TenancyValidator tenancyValidator;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats(User user, DashboardFilterDTO filter) {
        Map<String, Object> stats = new HashMap<>();

        UUID schoolId = filter != null ? filter.getSchoolId() : null;
        UUID demandId = filter != null ? filter.getDemandId() : null;

        if (user.getUserRole() == UserRole.PLATFORM_ADMIN) {
            stats.put("schoolsCount", schoolRepository.count());
            stats.put("universitiesCount", universityRepository.count());
            stats.put("activeStudentsCount", studentRepository.countByStatusAndFilters(StudentStatus.VALIDATED, null, schoolId, demandId));
            stats.put("waitingDemands", demandRepository.countByStatusAndFilters(DemandStatus.WAITING, schoolId, demandId));
            stats.put("finishedDemands", demandRepository.countByStatusAndFilters(DemandStatus.FINISHED, schoolId, demandId));

            // Indicadores extras de Feedback (Sempre globais para Admin)
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(null, null, null));
            stats.put("classAverageScore", classEvaluationRepository.getAverageScoreByFilters(null, null, null));

            // Indicadores extras para Admin quando filtrado
            if (schoolId != null || demandId != null) {
                stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(null, schoolId, demandId, null));
                stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(null, schoolId, demandId, null));
            }
        } else if (user instanceof UniversityAdmin admin) {
            if (admin.getUniversity() == null) {
                return DashboardStatsDTO.builder().statistics(stats).build();
            }
            var universityId = admin.getUniversity().getId();
            tenancyValidator.validateUniversity(universityId);

            stats.put("myStudentsCount", studentRepository.countByStatusAndFilters(StudentStatus.VALIDATED, universityId, schoolId, demandId));
            stats.put("activeStudentsCount", demandRepository.countActiveStudentsByUniversityId(universityId));

            BigDecimal totalHours = internshipFacade.getHoursByUniversity(universityId).students().stream()
                    .map(com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO::totalHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalHours", totalHours);

            stats.put("completedDemands", demandRepository.countFinishedDemandsByUniversityId(universityId));

            // Indicadores de Feedback (Sem filtros dinâmicos de escola/demanda)
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(universityId, null, null));
            stats.put("classAverageScore", classEvaluationRepository.getAverageScoreByFilters(universityId, null, null));

            // Gráfico para Universidade filtrado
            stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(universityId, schoolId, demandId, null));

            // Distribuição Nine Box filtrada
            stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(universityId, schoolId, demandId, null));
        } else if (user instanceof SchoolDirector director) {
            if (director.getSchool() == null) {
                return DashboardStatsDTO.builder().statistics(stats).build();
            }
            var userSchoolId = director.getSchool().getId();
            tenancyValidator.validateSchool(userSchoolId);

            // O diretor já está restrito à sua escola, mas pode filtrar por demanda
            stats.put("myDemandsCount", demandRepository.countByFilters(userSchoolId, demandId));
            stats.put("openVacanciesCount", demandRepository.countByStatusAndFilters(DemandStatus.WAITING, userSchoolId, demandId));
            stats.put("ongoingClassesCount", demandRepository.countByStatusAndFilters(DemandStatus.ONGOING, userSchoolId, demandId));
            stats.put("successfulDemandsCount", demandRepository.countByStatusAndFilters(DemandStatus.FINISHED, userSchoolId, demandId));
            stats.put("validatedLessonsCount", lessonRepository.countValidatedByFilters(userSchoolId, demandId));

            // Indicadores de Feedback (Sem filtro dinâmico de demanda)
            stats.put("schoolAverageScore", classEvaluationRepository.getAverageScoreByFilters(null, userSchoolId, null));
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(null, userSchoolId, null));

            // Gráfico para Diretor filtrado
            stats.put("lessonsByPeriodAndDemand", lessonRepository.countBySchoolAndPeriodAndDemandFiltered(userSchoolId, demandId));

            // Distribuição Nine Box filtrada
            stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(null, userSchoolId, demandId, null));
        } else if (user instanceof Student student) {
            tenancyValidator.validateUserAccess(student.getId());

            BigDecimal myHours = internshipFacade.getHoursByStudent(student.getId()).totalHours();
            stats.put("myHours", myHours);
            
            stats.put("applicationsCount", demandRepository.countByCandidateId(student.getId()));
            stats.put("interestAreas", student.getInterestAreas());

            // Gráfico para Estudante filtrado (embora estudante geralmente só veja o dele, demandId pode ser útil se ele estiver em várias)
            stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(null, schoolId, demandId, student.getId()));
            
            Optional<Demand> currentDemand = demandRepository.findFirstByStudentIdAndStatus(student.getId(), DemandStatus.ONGOING);
            currentDemand.ifPresent(demand -> stats.put("currentProject", Map.of(
                "id", demand.getId(),
                "title", demand.getTitle(),
                "schoolName", demand.getSchool().getName(),
                "status", demand.getStatus()
            )));
        }

        return DashboardStatsDTO.builder().statistics(stats).build();
    }
}
