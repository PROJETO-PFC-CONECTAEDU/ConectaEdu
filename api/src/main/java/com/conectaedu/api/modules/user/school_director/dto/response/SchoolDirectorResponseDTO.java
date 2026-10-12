package com.conectaedu.api.modules.user.school_director.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchoolDirectorResponseDTO(
        UUID id,
        String name,
        String email,
        UUID schoolId,
        String schoolName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
