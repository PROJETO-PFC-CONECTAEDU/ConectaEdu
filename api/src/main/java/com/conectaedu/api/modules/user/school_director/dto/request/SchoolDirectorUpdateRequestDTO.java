package com.conectaedu.api.modules.user.school_director.dto.request;

import jakarta.validation.constraints.Email;

import java.util.UUID;

public record SchoolDirectorUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email,

        UUID schoolId
) {
}
