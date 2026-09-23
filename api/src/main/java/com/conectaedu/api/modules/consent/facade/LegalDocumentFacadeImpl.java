package com.conectaedu.api.modules.consent.facade;

import com.conectaedu.api.modules.consent.dto.request.LegalDocumentCreationRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentCreationResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.modules.consent.interfaces.ILegalDocumentFacade;
import com.conectaedu.api.modules.consent.service.*;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LegalDocumentFacadeImpl implements ILegalDocumentFacade {

    private final LegalDocumentCreationService legalDocumentCreationService;
    private final LegalDocumentListByService legalDocumentListByService;

    @Override
    public LegalDocumentCreationResponseDTO createDocument(LegalDocumentCreationRequestDTO request) {
        return legalDocumentCreationService.createDocument(request);
    }

    @Override
    public LegalDocumentResponseDTO getCurrentDocument(LegalDocumentType documentType) {
        return legalDocumentListByService.listCurrentByType(documentType);
    }
}
