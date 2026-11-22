package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.lesson.support.LessonRules;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandRecommendationService {

    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final DemandMapper demandMapper;
    private final TenancyValidator tenancyValidator;

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getRecommendedDemands(UUID studentId) {
        Student student = studentRepository.findWithInterestAreasById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado"));

        tenancyValidator.validateStudentAccess(student);

        List<Demand> availableDemands = demandRepository.findByStatus(DemandStatus.WAITING);

        return availableDemands.stream()
                .map(demand -> {
                    double distance = 0;
                    if (student.getLatitude() != null && student.getLongitude() != null &&
                            demand.getSchool() != null && demand.getSchool().getLatitude() != 0) {
                        distance = calculateDistance(
                                student.getLatitude(), student.getLongitude(),
                                demand.getSchool().getLatitude(), demand.getSchool().getLongitude()
                        );
                    }
                    double score = calculateScore(student, demand, distance);
                    
                    boolean hasConflict = lessonRepository.existsOverlapBetweenDemandAndStudent(
                            demand.getId(), studentId, LessonRules.BUSY_STATUSES
                    );
                    
                    return new ScoredDemand(demand, score, distance, hasConflict);
                })
                .sorted(Comparator.comparingDouble(ScoredDemand::score).reversed())
                .map(sd -> demandMapper.mapToStudentDTO(sd.demand(), sd.score(), sd.distance(), false, false, sd.hasConflict()))
                .collect(Collectors.toList());
    }

    private double calculateScore(Student student, Demand demand, double distance) {
        double score = 0;
        //Estudante: possui Matemática entre suas áreas de interesse.
        //Demanda: reforço escolar de Matemática.
        //Distância até a escola: 2 km.
        //Afinidade: 60 pontos.
        //Proximidade: 40 - (2 × 5) = 30 pontos.
        //Total: 90 pontos.

        // 1. Afinidade de interesse (60 pontos)
        if (student.getInterestAreas() != null && demand.getSubject() != null) {
            if (student.getInterestAreas().stream().anyMatch(area ->
                    area.equalsIgnoreCase(demand.getSubject()))) {
                score += 60;
            }
        }

        // 2. Proximidade geográfica (40 pontos)
        if (distance >= 0) {
            score += Math.max(0, 40 - (distance * 5));
        }

        return Math.min(score, 100);
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

    private record ScoredDemand(Demand demand, double score, double distance, boolean hasConflict) {
    }
}
