package com.conectaedu.api.modules.consent.dto.request;

import com.conectaedu.api.shared.enums.LegalDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LegalDocumentCreationRequestDTO(

        @NotNull(message = "Tipo do documento é obrigatório")
        LegalDocumentType documentType,

        //Limite de três dígitos em cada parte: cabe na coluna e não estoura a
        //conversão para número feita na comparação de versões.
        @NotBlank(message = "Versão é obrigatória")
        @Pattern(regexp = "\\d{1,3}\\.\\d{1,3}", message = "Versão deve seguir o formato 1.0")
        String version,

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String title,

        @NotBlank(message = "Conteúdo é obrigatório")
        String content,

        @Size(max = 1000, message = "Resumo das alterações deve ter no máximo 1000 caracteres")
        String changeSummary,

        //Quem publica. Sairá daqui quando houver usuário autenticado.
        @NotNull(message = "Identificador do publicador é obrigatório")
        UUID publisherUserId
) {
}