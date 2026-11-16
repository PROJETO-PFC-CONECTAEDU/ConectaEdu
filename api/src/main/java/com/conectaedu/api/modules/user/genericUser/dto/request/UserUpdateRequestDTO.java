package com.conectaedu.api.modules.user.genericUser.dto.request;

import com.conectaedu.api.shared.enums.UserType;
import jakarta.validation.constraints.Email;

public record UserUpdateRequestDTO(
        String name,

        @Email(message = "Email inválido")
        String email,

        UserType userType,
        String universityCNPJ,
        String schoolCIE
) {
}
