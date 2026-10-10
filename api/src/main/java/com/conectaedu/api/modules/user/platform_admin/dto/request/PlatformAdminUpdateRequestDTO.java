package com.conectaedu.api.modules.user.platform_admin.dto.request;

import jakarta.validation.constraints.Email;

public record PlatformAdminUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email
) {
}
