package com.conectaedu.api.modules.consent.dto.response;

import java.util.List;
import java.util.UUID;

//Traz o documento completo para o front exibir o termo direto, sem outra chamada.
//missingDocumentTypes avisa quando um documento obrigatório nunca foi publicado.
public record ConsentStatusResponseDTO(
        UUID userId,
        boolean upToDate,
        List<LegalDocumentResponseDTO> pendingDocuments,
        List<String> missingDocumentTypes
) {
}
