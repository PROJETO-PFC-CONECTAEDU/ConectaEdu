package com.conectaedu.api.modules.university.dto.response;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.shared.enums.UniversityStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UniversityResponseDTO(
        UUID id,
        String name,
        String cnpj,
        String coordinator,
        String address,
        String status,
        boolean active,
        boolean canReceiveStudents,
        LocalDateTime validatedAt,
        String validationNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public UniversityResponseDTO(University university) {
        this(university.getId(),
                university.getName(),
                university.getCnpj(),
                university.getCoordinator(),
                university.getAddress(),
                university.getStatus() == null ? null : university.getStatus().name(),
                university.isActive(),
                university.getStatus() == UniversityStatus.APPROVED && university.isActive(),
                university.getValidatedAt(),
                university.getValidationNotes(),
                university.getCreatedAt(),
                university.getUpdatedAt());
    }
}
