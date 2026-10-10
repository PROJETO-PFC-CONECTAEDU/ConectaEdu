package com.conectaedu.api.modules.user.student.dto.request;

import com.conectaedu.api.shared.enums.UserType;
import jakarta.validation.constraints.Email;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record StudentUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email,

        UserType userType,

        UUID universityId,

        String availability,

        List<String> interestAreas
) {
}
