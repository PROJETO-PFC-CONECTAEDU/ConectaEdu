package com.conectaedu.api.modules.user.platform_admin.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlatformAdminResponseDTO(
        UUID id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
