package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import com.conectaedu.api.shared.exceptions.LegalDocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LegalDocumentListByService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Transactional(readOnly = true)
    public List<LegalDocumentResponseDTO> listAll() {
        return legalDocumentRepository.findAll(Sort.by(Sort.Direction.DESC, "publishedAt"))
                .stream()
                .map(LegalDocumentResponseDTO::new)
                .toList();
    }

    //Somente leitura
    @Transactional(readOnly = true)
    public LegalDocumentResponseDTO listCurrentByType(LegalDocumentType documentType) {
        LegalDocument document = legalDocumentRepository
                .findByDocumentTypeAndStatus(documentType, LegalDocumentStatus.PUBLISHED)
                .orElseThrow(() -> new LegalDocumentNotFoundException(
                        "Nenhuma versão publicada para " + documentType));
        return new LegalDocumentResponseDTO(document);
    }

    @Transactional(readOnly = true)
    public LegalDocumentResponseDTO getById(java.util.UUID id) {
        LegalDocument document = legalDocumentRepository.findById(id)
                .orElseThrow(() -> new LegalDocumentNotFoundException("Documento não encontrado!"));
        return new LegalDocumentResponseDTO(document);
    }
}