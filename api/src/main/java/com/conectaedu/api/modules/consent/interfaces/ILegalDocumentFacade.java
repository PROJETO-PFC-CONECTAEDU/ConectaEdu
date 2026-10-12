package com.conectaedu.api.modules.consent.interfaces;

import com.conectaedu.api.modules.consent.dto.request.LegalDocumentCreationRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentCreationResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.shared.enums.LegalDocumentType;

import java.util.List;

public interface ILegalDocumentFacade {

    LegalDocumentCreationResponseDTO createDocument(LegalDocumentCreationRequestDTO request);

    LegalDocumentResponseDTO getCurrentDocument(LegalDocumentType documentType);
    
    LegalDocumentResponseDTO getDocumentById(java.util.UUID id);

    List<LegalDocumentResponseDTO> listAll();
}