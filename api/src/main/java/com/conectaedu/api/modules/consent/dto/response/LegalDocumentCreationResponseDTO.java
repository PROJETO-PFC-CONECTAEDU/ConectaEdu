package com.conectaedu.api.modules.consent.dto.response;

import java.util.UUID;

public record LegalDocumentCreationResponseDTO(
        UUID id,
        String version,
        String message
) {
}
