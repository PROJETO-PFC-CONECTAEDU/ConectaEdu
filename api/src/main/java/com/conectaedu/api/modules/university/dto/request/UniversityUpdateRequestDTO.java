package com.conectaedu.api.modules.university.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UniversityUpdateRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Coordenador é obrigatório")
        String coordinator,

        @NotNull(message = "Endereço é obrigatório")
        String address
) {
}
