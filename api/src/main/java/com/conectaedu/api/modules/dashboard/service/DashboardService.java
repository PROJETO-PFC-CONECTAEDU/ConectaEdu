package com.conectaedu.api.modules.dashboard.service;

import com.conectaedu.api.modules.dashboard.dto.DashboardStatsDTO;
import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
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

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SchoolRepository schoolRepository;
    private final UniversityRepository universityRepository;
    private final StudentRepository studentRepository;
    private final DemandRepository demandRepository;
    private final LessonRepository lessonRepository;
    private final IInternshipFacade internshipFacade;
    private final TenancyValidator tenancyValidator;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        if (user.getUserRole() == UserRole.PLATFORM_ADMIN) {
            stats.put("schoolsCount", schoolRepository.count());
            stats.put("universitiesCount", universityRepository.count());
            stats.put("activeStudentsCount", studentRepository.countByStatus(StudentStatus.VALIDATED));
            stats.put("waitingDemands", demandRepository.countByStatus(DemandStatus.WAITING));
            stats.put("finishedDemands", demandRepository.countByStatus(DemandStatus.FINISHED));
        } else if (user instanceof UniversityAdmin admin) {
            if (admin.getUniversity() == null) {
                return DashboardStatsDTO.builder().statistics(stats).build();
            }
            var universityId = admin.getUniversity().getId();
            tenancyValidator.validateUniversity(universityId);

            stats.put("myStudentsCount", studentRepository.countByUniversityId(universityId));
            stats.put("activeStudentsCount", demandRepository.countActiveStudentsByUniversityId(universityId));

            BigDecimal totalHours = internshipFacade.getHoursByUniversity(universityId).students().stream()
                    .map(com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO::totalHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalHours", totalHours);

            stats.put("completedDemands", demandRepository.countFinishedDemandsByUniversityId(universityId));

            // Gráfico para Universidade: aulas por período
            stats.put("lessonsByPeriod", lessonRepository.countByUniversityAndPeriod(universityId));
        } else if (user instanceof SchoolDirector director) {
            if (director.getSchool() == null) {
                return DashboardStatsDTO.builder().statistics(stats).build();
            }
            var schoolId = director.getSchool().getId();
            tenancyValidator.validateSchool(schoolId);

            stats.put("myDemandsCount", demandRepository.countBySchoolId(schoolId));
            stats.put("openVacanciesCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.WAITING));
            stats.put("ongoingClassesCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.ONGOING));
            stats.put("successfulDemandsCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.FINISHED));

            // Gráfico para Diretor: barra por período e demanda
            stats.put("lessonsByPeriodAndDemand", lessonRepository.countBySchoolAndPeriodAndDemand(schoolId));
        } else if (user instanceof Student student) {
            tenancyValidator.validateUserAccess(student.getId());

            BigDecimal myHours = internshipFacade.getHoursByStudent(student.getId()).totalHours();
            stats.put("myHours", myHours);
            
            stats.put("applicationsCount", demandRepository.countByCandidateId(student.getId()));
            stats.put("interestAreas", student.getInterestAreas());

            // Gráfico para Estudante: aulas realizadas no período
            stats.put("lessonsByPeriod", lessonRepository.countByStudentAndPeriod(student.getId()));
            
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
