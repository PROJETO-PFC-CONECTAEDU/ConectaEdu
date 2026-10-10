package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.response.ConsentStatusResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.shared.enums.ConsentAction;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentVerificationService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final ConsentRecordRepository consentRecordRepository;

    // Leitura apenas
    @Transactional(readOnly = true)
    public ConsentStatusResponseDTO getConsentStatus(UUID userId) {

        List<LegalDocument> published = legalDocumentRepository
                .findByStatus(LegalDocumentStatus.PUBLISHED);

        //Pendente é toda versão em vigor que o usuário ainda não aceitou.
        List<LegalDocumentResponseDTO> pending = published
                .stream()
                .filter(document -> !consentRecordRepository
                        .existsByUser_IdAndLegalDocument_IdAndAction(
                                userId, document.getId(), ConsentAction.ACCEPTED))
                .map(LegalDocumentResponseDTO::new)
                .toList();

        return new ConsentStatusResponseDTO(
                userId, !pending.isEmpty(), pending, findMissingDocumentTypes(published));
    }

    //Tipo obrigatório que nunca foi publicado não vira pendência.

    private List<String> findMissingDocumentTypes(List<LegalDocument> published) {
        List<String> availableTypes = published.stream()
                .map(document -> document.getDocumentType().name())
                .toList();

        return Arrays.stream(LegalDocumentType.values())
                .map(LegalDocumentType::name)
                .filter(documentType -> !availableTypes.contains(documentType))
                .toList();
    }
}