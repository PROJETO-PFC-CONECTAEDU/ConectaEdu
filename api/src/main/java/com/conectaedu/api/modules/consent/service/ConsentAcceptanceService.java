package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.ConsentRecord;
import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.request.ConsentAcceptanceRequestDTO;
import com.conectaedu.api.modules.consent.dto.request.ConsentWithdrawalRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.enums.ConsentAction;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.exceptions.ConsentAlreadyGivenException;
import com.conectaedu.api.shared.exceptions.ConsentNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidLegalDocumentStatusException;
import com.conectaedu.api.shared.exceptions.LegalDocumentNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsentAcceptanceService {

    private final ConsentRecordRepository consentRecordRepository;
    private final LegalDocumentRepository legalDocumentRepository;
    private final UserRepository userRepository;

    //Registra o aceite de uma versão específica.
    @Transactional
    public ConsentRecordResponseDTO acceptDocument(ConsentAcceptanceRequestDTO request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        LegalDocument document = legalDocumentRepository.findById(request.legalDocumentId())
                .orElseThrow(() -> new LegalDocumentNotFoundException("Documento não encontrado!"));

        //Versão arquivada foi substituída, o aceite precisa ser da versão em vigor.
        if (document.getStatus() != LegalDocumentStatus.PUBLISHED) {
            throw new InvalidLegalDocumentStatusException(
                    "A versão " + document.getVersion() + " foi substituída. Aceite a versão em vigor.");
        }

        //Aceitar duas vezes a mesma versão inflaria o histórico sem significado.
        if (consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                user.getId(), document.getId(), ConsentAction.ACCEPTED)) {
            throw new ConsentAlreadyGivenException(
                    "Este usuário já aceitou a versão " + document.getVersion());
        }

        ConsentRecord record = new ConsentRecord();
        record.setUser(user);
        record.setLegalDocument(document);
        record.setAction(ConsentAction.ACCEPTED);
        record.setAcceptedAt(LocalDateTime.now());

        //Copia o hash do texto aceito, se o documento mudar, a divergência aparece.
        record.setAcceptedContentHash(document.getContentHash());

        consentRecordRepository.save(record);
        return new ConsentRecordResponseDTO(record);
    }

    //Revogação do aceite não é apagado, só recebe uma data de fim.
    @Transactional
    public ConsentRecordResponseDTO withdrawConsent(ConsentWithdrawalRequestDTO request) {

        ConsentRecord record = consentRecordRepository
                .findByUser_IdAndLegalDocument_IdAndAction(
                        request.userId(), request.legalDocumentId(), ConsentAction.ACCEPTED)
                .orElseThrow(() -> new ConsentNotFoundException(
                        "Não há aceite vigente deste documento para este usuário."));

        record.setAction(ConsentAction.WITHDRAWN);
        record.setWithdrawnAt(LocalDateTime.now());

        consentRecordRepository.save(record);
        return new ConsentRecordResponseDTO(record);
    }
}
