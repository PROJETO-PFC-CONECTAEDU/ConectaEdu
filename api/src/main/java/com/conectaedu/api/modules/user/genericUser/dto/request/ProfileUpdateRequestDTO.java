package com.conectaedu.api.modules.user.genericUser.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name
) {
}
