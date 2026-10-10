package com.conectaedu.api.modules.demand.mapper;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DemandMapper {

    public DemandResponseDTO mapToResponseDTO(Demand demand) {
        return new DemandResponseDTO(
                demand.getId(),
                demand.getTitle(),
                demand.getDescription(),
                demand.getSubject(),
                demand.getGradeLevel(),
                demand.getPupilAmount(),
                demand.getStatus(),
                demand.getClassDate(),
                demand.getTotalHours(),
                demand.getRoom(),
                demand.getDifficultyLevel(),
                demand.isArchived(),
                demand.getPendingClassDate(),
                demand.getPendingRoom(),
                demand.getSchool().getId(),
                demand.getSchool().getName(),
                demand.getSchool().getAddress(),
                demand.getSchool().getLatitude(),
                demand.getSchool().getLongitude(),
                demand.getDirector().getId(),
                demand.getDirector().getName(),
                demand.getStudent() != null ? demand.getStudent().getId() : null,
                demand.getStudent() != null ? demand.getStudent().getName() : null,
                demand.getCandidates().stream()
                        .map(c -> new DemandResponseDTO.CandidateResponseDTO(c.getId(), c.getName()))
                        .collect(Collectors.toList()),
                demand.getStudentHistory().stream()
                        .map(h -> new DemandResponseDTO.HistoryResponseDTO(
                                h.getStudent().getId(),
                                h.getStudent().getName(),
                                h.isActive(),
                                h.getLinkedAt(),
                                h.getUnlinkedAt()
                        ))
                        .collect(Collectors.toList()),
                demand.getCreatedAt(),
                demand.getUpdatedAt()
        );
    }

    public DemandStudentDTO mapToStudentDTO(Demand demand) {
        return new DemandStudentDTO(
                demand.getId(),
                demand.getTitle(),
                demand.getDescription(),
                demand.getSubject(),
                demand.getGradeLevel(),
                demand.getPupilAmount(),
                demand.getStatus(),
                demand.getClassDate(),
                demand.getTotalHours(),
                demand.getRoom(),
                demand.getDifficultyLevel(),
                demand.isArchived(),
                demand.getSchool().getId(),
                demand.getSchool().getName(),
                demand.getSchool().getAddress(),
                demand.getSchool().getLatitude(),
                demand.getSchool().getLongitude(),
                demand.getDirector().getId(),
                demand.getDirector().getName(),
                demand.getStudent() != null ? demand.getStudent().getId() : null,
                demand.getStudent() != null ? demand.getStudent().getName() : null,
                demand.getCandidates().stream()
                        .map(c -> new DemandStudentDTO.CandidateResponseDTO(c.getId(), c.getName()))
                        .collect(Collectors.toList()),
                demand.getCreatedAt(),
                demand.getUpdatedAt()
        );
    }
}
