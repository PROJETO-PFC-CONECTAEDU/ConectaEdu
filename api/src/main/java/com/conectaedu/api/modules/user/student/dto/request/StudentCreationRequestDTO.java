package com.conectaedu.api.modules.user.student.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record StudentCreationRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String password,

        @NotNull(message = "ID da universidade é obrigatório")
        UUID universityId,

        @NotBlank(message = "Disponibilidade é obrigatória")
        String availability,

        @NotEmpty(message = "Área de interesse é obrigatória")
        List<String> interestAreas
) {
}
