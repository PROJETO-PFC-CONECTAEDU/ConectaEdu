package com.conectaedu.api.modules.consent.dto.response;

import com.conectaedu.api.modules.consent.domain.LegalDocument;

import java.time.LocalDateTime;
import java.util.UUID;

public record LegalDocumentResponseDTO(
        UUID id,
        String documentType,
        String version,
        String title,
        String content,
        String contentHash,
        String status,
        String changeSummary,
        LocalDateTime publishedAt,
        LocalDateTime archivedAt
) {

    public LegalDocumentResponseDTO(LegalDocument document) {
        this(document.getId(),
                document.getDocumentType() == null ? null : document.getDocumentType().name(),
                document.getVersion(),
                document.getTitle(),
                document.getContent(),
                document.getContentHash(),
                document.getStatus() == null ? null : document.getStatus().name(),
                document.getChangeSummary(),
                document.getPublishedAt(),
                document.getArchivedAt());
    }
}
