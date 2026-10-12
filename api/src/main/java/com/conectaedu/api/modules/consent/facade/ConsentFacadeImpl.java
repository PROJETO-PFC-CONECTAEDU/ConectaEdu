package com.conectaedu.api.modules.consent.facade;

import com.conectaedu.api.modules.consent.dto.request.ConsentAcceptanceRequestDTO;
import com.conectaedu.api.modules.consent.dto.request.ConsentWithdrawalRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentStatusResponseDTO;
import com.conectaedu.api.modules.consent.interfaces.IConsentFacade;
import com.conectaedu.api.modules.consent.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConsentFacadeImpl implements IConsentFacade {

    private final ConsentAcceptanceService consentAcceptanceService;
    private final ConsentVerificationService consentVerificationService;
    private final ConsentListService consentListService;

    @Override
    public ConsentRecordResponseDTO acceptDocument(ConsentAcceptanceRequestDTO request) {
        return consentAcceptanceService.acceptDocument(request);
    }

    @Override
    public ConsentRecordResponseDTO withdrawConsent(ConsentWithdrawalRequestDTO request) {
        return consentAcceptanceService.withdrawConsent(request);
    }

    @Override
    public ConsentStatusResponseDTO getConsentStatus(UUID userId) {
        return consentVerificationService.getConsentStatus(userId);
    }

    @Override
    public List<ConsentRecordResponseDTO> getConsentsByUser(UUID userId) {
        return consentListService.listByUser(userId);
    }
}
