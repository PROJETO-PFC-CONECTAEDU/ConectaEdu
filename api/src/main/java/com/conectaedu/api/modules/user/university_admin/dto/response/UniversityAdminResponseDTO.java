package com.conectaedu.api.modules.user.university_admin.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UniversityAdminResponseDTO(
        UUID id,
        String name,
        String email,
        UUID universityId,
        String universityName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
