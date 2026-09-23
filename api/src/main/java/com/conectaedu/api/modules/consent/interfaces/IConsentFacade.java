package com.conectaedu.api.modules.consent.interfaces;

import com.conectaedu.api.modules.consent.dto.request.ConsentAcceptanceRequestDTO;
import com.conectaedu.api.modules.consent.dto.request.ConsentWithdrawalRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentStatusResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IConsentFacade {

    ConsentRecordResponseDTO acceptDocument(ConsentAcceptanceRequestDTO request);

    ConsentRecordResponseDTO withdrawConsent(ConsentWithdrawalRequestDTO request);

    ConsentStatusResponseDTO getConsentStatus(UUID userId);

    List<ConsentRecordResponseDTO> getConsentsByUser(UUID userId);
}
