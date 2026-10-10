package com.conectaedu.api.modules.university.dto.request;

public record UniversityValidationRequestDTO(

        //Parecer do administrador. Opcional na aprovação, obrigatório na recusa.
        String validationNotes
) {
}
