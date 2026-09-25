package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import com.conectaedu.api.shared.exceptions.LegalDocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalDocumentListByService {

    private final LegalDocumentRepository legalDocumentRepository;

    //Somente leitura
    @Transactional(readOnly = true)
    public LegalDocumentResponseDTO listCurrentByType(LegalDocumentType documentType) {
        LegalDocument document = legalDocumentRepository
                .findByDocumentTypeAndStatus(documentType, LegalDocumentStatus.PUBLISHED)
                .orElseThrow(() -> new LegalDocumentNotFoundException(
                        "Nenhuma versão publicada para " + documentType));
        return new LegalDocumentResponseDTO(document);
    }
}