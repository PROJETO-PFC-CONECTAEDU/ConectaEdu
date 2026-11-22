package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandForStudentService {
    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final DemandMapper demandMapper;
    private final TenancyValidator tenancyValidator;

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getAvailableDemandsForStudents() {
        Student student = null;
        UUID authUserId = null;
        try {
            authUserId = SecurityContextHolder.getAuthenticatedUserId();
            student = studentRepository.findById(authUserId).orElse(null);
            if (student != null) {
                tenancyValidator.validateStudentAccess(student);
            }
        } catch (Exception ignored) {}

        final Student finalStudent = student;
        final UUID finalUserId = authUserId;
        
        return demandRepository.findByStatus(DemandStatus.WAITING).stream()
                .map(demand -> {
                    Double distance = null;
                    if (finalStudent != null && finalStudent.getLatitude() != null &&
                            demand.getSchool() != null && demand.getSchool().getLatitude() != 0) {
                        distance = calculateDistance(
                                finalStudent.getLatitude(), finalStudent.getLongitude(),
                                demand.getSchool().getLatitude(), demand.getSchool().getLongitude()
                        );
                    }
                    
                    boolean hasConflict = false;
                    if (finalUserId != null) {
                        hasConflict = lessonRepository.existsOverlapBetweenDemandAndStudent(
                                demand.getId(), finalUserId, LessonRules.BUSY_STATUSES
                        );
                    }
                    
                    return demandMapper.mapToStudentDTO(demand, null, distance, false, false, hasConflict);
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
