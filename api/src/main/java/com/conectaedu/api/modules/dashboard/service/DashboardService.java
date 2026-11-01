package com.conectaedu.api.modules.dashboard.service;

import com.conectaedu.api.modules.dashboard.dto.DashboardStatsDTO;
import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public DashboardStatsDTO getStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        if (user.getUserRole() == UserRole.PLATFORM_ADMIN) {
            stats.put("schoolsCount", schoolRepository.count());
            stats.put("universitiesCount", universityRepository.count());
            stats.put("activeStudentsCount", studentRepository.countByStatus(StudentStatus.VALIDATED));
            stats.put("waitingDemands", demandRepository.countByStatus(DemandStatus.WAITING));
            stats.put("finishedDemands", demandRepository.countByStatus(DemandStatus.FINISHED));
        } else if (user instanceof UniversityAdmin admin) {
            var universityId = admin.getUniversity().getId();
            stats.put("myStudentsCount", studentRepository.countByUniversityId(universityId));
            stats.put("activeStudentsCount", demandRepository.countActiveStudentsByUniversityId(universityId));
            stats.put("totalHours", Optional.ofNullable(demandRepository.sumTotalHoursByUniversityId(universityId)).orElse(0.0));
            stats.put("completedDemands", demandRepository.countFinishedDemandsByUniversityId(universityId));
        } else if (user instanceof SchoolDirector director) {
            var schoolId = director.getSchool().getId();
            stats.put("myDemandsCount", demandRepository.countBySchoolId(schoolId));
            stats.put("openVacanciesCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.WAITING));
            stats.put("ongoingClassesCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.ONGOING));
            stats.put("successfulDemandsCount", demandRepository.countBySchoolIdAndStatus(schoolId, DemandStatus.FINISHED));
        } else if (user instanceof Student student) {
            stats.put("myHours", Optional.ofNullable(demandRepository.sumTotalHoursByStudentId(student.getId())).orElse(0.0));
            stats.put("applicationsCount", demandRepository.countByCandidateId(student.getId()));
            stats.put("interestAreas", student.getInterestAreas());
            
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
