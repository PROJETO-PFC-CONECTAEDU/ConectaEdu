package com.conectaedu.api.modules.consent.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConsentWithdrawalRequestDTO(

        @NotNull(message = "Identificador do usuário é obrigatório")
        UUID userId,

        @NotNull(message = "Identificador do documento é obrigatório")
        UUID legalDocumentId
) {
}