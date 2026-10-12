package com.conectaedu.api.modules.consent.dto.response;

import com.conectaedu.api.modules.consent.domain.ConsentRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsentRecordResponseDTO(
        UUID id,
        UUID userId,
        UUID legalDocumentId,
        String documentType,
        String documentVersion,
        String action,
        LocalDateTime acceptedAt,
        LocalDateTime withdrawnAt,
        String acceptedContentHash
) {

    public ConsentRecordResponseDTO(ConsentRecord record) {
        this(record.getId(),
                record.getUser() == null ? null : record.getUser().getId(),
                record.getLegalDocument() == null ? null : record.getLegalDocument().getId(),
                record.getLegalDocument() == null ? null
                        : record.getLegalDocument().getDocumentType().name(),
                record.getLegalDocument() == null ? null : record.getLegalDocument().getVersion(),
                record.getAction() == null ? null : record.getAction().name(),
                record.getAcceptedAt(),
                record.getWithdrawnAt(),
                record.getAcceptedContentHash());
    }
}