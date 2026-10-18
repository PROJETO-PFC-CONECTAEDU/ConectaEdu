package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
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
    private final DemandMapper demandMapper;

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getRecommendedDemands(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

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
                    return new ScoredDemand(demand, score, distance);
                })
                .sorted(Comparator.comparingDouble(ScoredDemand::score).reversed())
                .map(sd -> demandMapper.mapToStudentDTO(sd.demand(), sd.score(), sd.distance()))
                .collect(Collectors.toList());
    }

    private double calculateScore(Student student, Demand demand, double distance) {
        double score = 0;

        // 1. Afinidade de Interesse (Peso Alto: 100)
        if (student.getInterestAreas() != null && demand.getSubject() != null) {
            if (student.getInterestAreas().stream().anyMatch(area ->
                    area.equalsIgnoreCase(demand.getSubject()))) {
                score += 100;
            }
        }

        // 2. Proximidade Geográfica (Peso Médio: até 80)
        if (distance > 0) {
            // Ex: 80 pontos se < 2km, diminui conforme a distância aumenta
            score += Math.max(0, 80 - (distance * 5));
        }

        // 3. Correspondência com o Curso (Peso Médio: 50)
        if (student.getCourse() != null && demand.getSubject() != null &&
                demand.getSubject().toLowerCase().contains(student.getCourse().toLowerCase())) {
            score += 50;
        }

        // 4. Recência (Peso Baixo: 20)
        if (demand.getCreatedAt() != null && demand.getCreatedAt().isAfter(LocalDateTime.now().minusDays(2))) {
            score += 20;
        }

        return score;
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

    private record ScoredDemand(Demand demand, double score, double distance) {
    }
}
