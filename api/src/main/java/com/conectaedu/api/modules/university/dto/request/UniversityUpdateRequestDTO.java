package com.conectaedu.api.modules.university.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


public record UniversityUpdateRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Coordenador é obrigatório")
        String coordinator,

        @NotNull(message = "Endereço é obrigatório")
        String address,

        //Deixei opcional de propósito, quem não manda o campo não apaga a carga já configurada.

        @Min(value = 1, message = "Carga horária de estágio deve ser maior que zero")
        Integer requiredInternshipHours
) {
}
