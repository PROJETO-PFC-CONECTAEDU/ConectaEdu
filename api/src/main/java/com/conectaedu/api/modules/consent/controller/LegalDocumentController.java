package com.conectaedu.api.modules.consent.controller;

import com.conectaedu.api.modules.consent.dto.request.LegalDocumentCreationRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentCreationResponseDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentResponseDTO;
import com.conectaedu.api.modules.consent.interfaces.ILegalDocumentFacade;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final ILegalDocumentFacade legalDocumentFacade;

    //Publica a nova versão e arquiva a anterior do mesmo tipo.
    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<LegalDocumentCreationResponseDTO> createDocument(
            @RequestBody @Valid LegalDocumentCreationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(legalDocumentFacade.createDocument(request));
    }

    //Versão em vigor, é o que o front exibe no cadastro e no rodapé.
    @GetMapping("/current/{documentType}")
    public ResponseEntity<LegalDocumentResponseDTO> getCurrentDocument(
            @PathVariable LegalDocumentType documentType) {
        return ResponseEntity.ok(legalDocumentFacade.getCurrentDocument(documentType));
    }
}