package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandGetService {
    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final DemandMapper demandMapper;

    @Transactional(readOnly = true)
    public DemandResponseDTO getDemand(UUID id) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));
        
        Double distance = null;
        try {
            UUID userId = SecurityContextHolder.getAuthenticatedUserId();
            Student student = studentRepository.findById(userId).orElse(null);
            if (student != null && student.getLatitude() != null && demand.getSchool() != null && demand.getSchool().getLatitude() != 0) {
                distance = calculateDistance(
                        student.getLatitude(), student.getLongitude(),
                        demand.getSchool().getLatitude(), demand.getSchool().getLongitude()
                );
            }
        } catch (Exception ignored) {}

        return demandMapper.mapToResponseDTO(demand, distance);
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

    @Transactional(readOnly = true)
    public List<DemandResponseDTO> getAllDemands() {
        return demandRepository.findAll().stream()
                .map(demandMapper::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemandResponseDTO> getDemandsByStatus(DemandStatus status) {
        return demandRepository.findByStatus(status).stream()
                .map(demandMapper::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getDemandsByStatusAndStudentId(DemandStatus status, UUID studentId) {
        return demandRepository.findByStatusAndStudentId(status, studentId).stream()
                .map(demandMapper::mapToStudentDTO)
                .toList();
    }
}
