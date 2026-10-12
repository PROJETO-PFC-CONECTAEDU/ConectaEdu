package com.conectaedu.api.modules.user.genericUser.facade.dto.request;

import com.conectaedu.api.shared.enums.UserType;
import jakarta.validation.constraints.Email;

public record UserUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email,

        UserType userType
) {
}
