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

import com.conectaedu.api.modules.internship.support.InternshipRules;
import com.conectaedu.api.modules.lesson.domain.Lesson;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        UUID studentId = filter != null ? filter.getStudentId() : null;
        java.time.LocalDate startDate = filter != null ? filter.getStartDate() : null;
        java.time.LocalDate endDate = filter != null ? filter.getEndDate() : null;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        if (user.getUserRole() == UserRole.PLATFORM_ADMIN) {
            stats.put("schoolsCount", schoolRepository.count());
            stats.put("universitiesCount", universityRepository.count());
            stats.put("activeStudentsCount", studentRepository.countByStatusAndFilters(StudentStatus.VALIDATED, null, schoolId, demandId));
            stats.put("waitingDemands", demandRepository.countByStatusAndFilters(DemandStatus.WAITING, schoolId, demandId, startDateTime, endDateTime));
            stats.put("finishedDemands", demandRepository.countByStatusAndFilters(DemandStatus.FINISHED, schoolId, demandId, startDateTime, endDateTime));

            // Indicadores extras de Feedback (Sempre globais para Admin, a menos que filtrado)
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(null, schoolId, demandId, studentId));
            stats.put("classAverageScore", classEvaluationRepository.getAverageScoreByFilters(null, schoolId, demandId, studentId));

            // Indicadores extras para Admin quando filtrado
            stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(null, schoolId, demandId, studentId, startDate, endDate));
            stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(null, schoolId, demandId, studentId, startDate, endDate));
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

            // Indicadores de Feedback
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(universityId, schoolId, demandId, studentId));
            stats.put("classAverageScore", classEvaluationRepository.getAverageScoreByFilters(universityId, schoolId, demandId, studentId));

            // Gráfico para Universidade filtrado
            stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(universityId, schoolId, demandId, studentId, startDate, endDate));

            // Distribuição Nine Box filtrada
            stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(universityId, schoolId, demandId, studentId, startDate, endDate));
        } else if (user instanceof SchoolDirector director) {
            if (director.getSchool() == null) {
                return DashboardStatsDTO.builder().statistics(stats).build();
            }
            var userSchoolId = director.getSchool().getId();
            tenancyValidator.validateSchool(userSchoolId);

            // O diretor já está restrito à sua escola, mas pode filtrar por demanda e período
            stats.put("myDemandsCount", demandRepository.countByFilters(userSchoolId, demandId, startDateTime, endDateTime));
            stats.put("openVacanciesCount", demandRepository.countByStatusAndFilters(DemandStatus.WAITING, userSchoolId, demandId, startDateTime, endDateTime));
            stats.put("ongoingClassesCount", demandRepository.countByStatusAndFilters(DemandStatus.ONGOING, userSchoolId, demandId, startDateTime, endDateTime));
            stats.put("successfulDemandsCount", demandRepository.countByStatusAndFilters(DemandStatus.FINISHED, userSchoolId, demandId, startDateTime, endDateTime));
            stats.put("validatedLessonsCount", lessonRepository.countValidatedByFilters(userSchoolId, demandId, startDateTime, endDateTime));

            // Indicadores de Feedback
            stats.put("schoolAverageScore", classEvaluationRepository.getAverageScoreByFilters(null, userSchoolId, demandId, studentId));
            stats.put("studentsAverageScore", studentEvaluationRepository.getAverageScoreByFilters(null, userSchoolId, demandId, studentId));

            // Gráfico para Diretor filtrado
            stats.put("lessonsByPeriodAndDemand", lessonRepository.countBySchoolAndPeriodAndDemandFiltered(userSchoolId, demandId, startDate, endDate));

            // Distribuição Nine Box filtrada
            stats.put("nineBoxDistribution", lessonRepository.countNineBoxByFilters(null, userSchoolId, demandId, studentId, startDate, endDate));
        } else if (user instanceof Student student) {
            tenancyValidator.validateUserAccess(student.getId());

            BigDecimal myHours;
            if (startDate != null || endDate != null) {
                long totalMinutes = lessonRepository.findValidatedByStudentAndPeriod(student.getId(), startDateTime, endDateTime)
                        .stream()
                        .mapToLong(l -> l.getDuration().toMinutes())
                        .sum();
                myHours = InternshipRules.toHours(totalMinutes);
            } else {
                myHours = internshipFacade.getHoursByStudent(student.getId()).totalHours();
            }
            stats.put("myHours", myHours);
            
            stats.put("applicationsCount", demandRepository.countByCandidateId(student.getId()));
            stats.put("interestAreas", student.getInterestAreas());

            // Gráfico para Estudante filtrado
            stats.put("lessonsByPeriod", lessonRepository.countByFiltersAndPeriod(null, schoolId, demandId, student.getId(), startDate, endDate));
            
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
