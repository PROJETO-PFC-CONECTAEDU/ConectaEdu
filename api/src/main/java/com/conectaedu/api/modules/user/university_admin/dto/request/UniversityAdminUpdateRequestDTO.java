package com.conectaedu.api.modules.user.university_admin.dto.request;

import jakarta.validation.constraints.Email;

import java.util.UUID;

public record UniversityAdminUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email,

        UUID universityId
) {
}
