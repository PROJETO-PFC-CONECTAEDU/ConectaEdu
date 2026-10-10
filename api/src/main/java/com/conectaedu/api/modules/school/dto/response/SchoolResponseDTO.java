package com.conectaedu.api.modules.school.dto.response;

import com.conectaedu.api.modules.school.domain.School;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchoolResponseDTO(
        UUID id,
        String name,
        String cie,
        String director,
        String address,
        Double latitude,
        Double longitude,
        String status,
        boolean active,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public SchoolResponseDTO(School school) {
        this(school.getId(),
                school.getName(),
                school.getCie(),
                school.getDirector(),
                school.getAddress(),
                school.getLatitude(),
                school.getLongitude(),
                school.getStatus() == null ? null : school.getStatus().name(),
                school.isActive(),
                school.getCreatedAt(),
                school.getUpdatedAt());
    }
}
