package com.conectaedu.api.modules.school.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SchoolCreationRequestDTO(
        @NotBlank(message = "Nome da escola é obrigatório")
        String name,

        @NotBlank(message = "Código identificador da escola é obrigatório")
        String cie,

        @NotBlank(message = "Nome do diretor é obrigatório")
        String director,

        @NotBlank(message = "O endereço é obrigatório")
        String address


) {
}
