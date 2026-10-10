package com.conectaedu.api.modules.demand.dto.response;

import com.conectaedu.api.shared.enums.DemandStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DemandResponseDTO(
        UUID id,
        String title,
        String description,
        String subject,
        String gradeLevel,
        Integer pupilAmount,
        DemandStatus status,
        LocalDateTime classDate,
        String totalHours,
        String room,
        String difficultyLevel,
        boolean isArchived,
        LocalDateTime pendingClassDate,
        String pendingRoom,
        UUID schoolId,
        String schoolName,
        String schoolAddress,
        Double schoolLatitude,
        Double schoolLongitude,
        UUID directorId,
        String directorName,
        UUID studentId,
        String studentName,
        List<CandidateResponseDTO> candidates,
        List<HistoryResponseDTO> studentHistory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CandidateResponseDTO(UUID id, String name) {}
    public record HistoryResponseDTO(UUID studentId, String studentName, boolean active, LocalDateTime linkedAt, LocalDateTime unlinkedAt) {}
}
