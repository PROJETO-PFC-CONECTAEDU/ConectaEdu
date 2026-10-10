package com.conectaedu.api.modules.consent.controller;

import com.conectaedu.api.modules.consent.dto.request.ConsentAcceptanceRequestDTO;
import com.conectaedu.api.modules.consent.dto.request.ConsentWithdrawalRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentStatusResponseDTO;
import com.conectaedu.api.modules.consent.interfaces.IConsentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
public class ConsentController {

    private final IConsentFacade consentFacade;

    //Registra o aceite da versão em vigor.
    @PostMapping
    public ResponseEntity<ConsentRecordResponseDTO> acceptDocument(
            @RequestBody @Valid ConsentAcceptanceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consentFacade.acceptDocument(request));
    }

    //Revogação do aceite. O registro anterior não é apagado.
    @PatchMapping("/withdraw")
    public ResponseEntity<ConsentRecordResponseDTO> withdrawConsent(
            @RequestBody @Valid ConsentWithdrawalRequestDTO request) {
        return ResponseEntity.ok(consentFacade.withdrawConsent(request));
    }

    //Chamado após o login, diz se falta aceitar alguma versão em vigor.
    @GetMapping("/status/{userId}")
    public ResponseEntity<ConsentStatusResponseDTO> getConsentStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(consentFacade.getConsentStatus(userId));
    }

    //Histórico do titular, atende ao direito de acesso do art. 18, II.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConsentRecordResponseDTO>> getConsentsByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(consentFacade.getConsentsByUser(userId));
    }
}
