package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.response.ConsentStatusResponseDTO;
import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.shared.enums.ConsentAction;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//Regras, pendencia e toda versao em vigor que o usuario ainda nao aceitou.

@ExtendWith(MockitoExtension.class)
class ConsentVerificationServiceTest {

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @Mock
    private ConsentRecordRepository consentRecordRepository;

    @InjectMocks
    private ConsentVerificationService consentVerificationService;

    private LegalDocument publicado(LegalDocumentType tipo) {
        LegalDocument documento = new LegalDocument();
        documento.setId(UUID.randomUUID());
        documento.setDocumentType(tipo);
        documento.setVersion("1.0");
        documento.setStatus(LegalDocumentStatus.PUBLISHED);
        return documento;
    }

    // Usuario em dia com os dois documentos em vigor.
    @Test
    void naoDeveExigirConsentimentoQuandoUsuarioAceitouTodosOsDocumentos() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        LegalDocument termos = publicado(LegalDocumentType.TERMS_OF_USE);
        LegalDocument politica = publicado(LegalDocumentType.PRIVACY_POLICY);
        when(legalDocumentRepository.findByStatus(LegalDocumentStatus.PUBLISHED))
                .thenReturn(List.of(termos, politica));
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, termos.getId(), ConsentAction.ACCEPTED)).thenReturn(true);
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, politica.getId(), ConsentAction.ACCEPTED)).thenReturn(true);

        // execucao
        ConsentStatusResponseDTO status = consentVerificationService.getConsentStatus(usuarioId);

        // verificacao
        assertFalse(status.needsConsent());
        assertTrue(status.pendingDocuments().isEmpty());
        assertTrue(status.missingDocumentTypes().isEmpty());
    }

    //Falta aceitar a politica, o acesso nao pode ser liberado.
    @Test
    void deveExigirConsentimentoQuandoFaltaAceitarUmDocumento() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        LegalDocument termos = publicado(LegalDocumentType.TERMS_OF_USE);
        LegalDocument politica = publicado(LegalDocumentType.PRIVACY_POLICY);
        when(legalDocumentRepository.findByStatus(LegalDocumentStatus.PUBLISHED))
                .thenReturn(List.of(termos, politica));
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, termos.getId(), ConsentAction.ACCEPTED)).thenReturn(true);
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, politica.getId(), ConsentAction.ACCEPTED)).thenReturn(false);

        // execucao
        ConsentStatusResponseDTO status = consentVerificationService.getConsentStatus(usuarioId);

        // verificacao
        assertTrue(status.needsConsent());
        assertEquals(1, status.pendingDocuments().size());
        assertEquals(LegalDocumentType.PRIVACY_POLICY.name(), status.pendingDocuments().get(0).documentType());
    }

        //Base vazia nao trava o usuario, mas acusa que a plataforma esqueceu de publicar os dois documentos.
    @Test
    void deveApontarTiposAusentesQuandoNenhumDocumentoFoiPublicado() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        when(legalDocumentRepository.findByStatus(LegalDocumentStatus.PUBLISHED)).thenReturn(List.of());

        // execucao
        ConsentStatusResponseDTO status = consentVerificationService.getConsentStatus(usuarioId);

        // verificacao
        assertFalse(status.needsConsent());
        assertTrue(status.pendingDocuments().isEmpty());
        assertEquals(2, status.missingDocumentTypes().size());
        assertTrue(status.missingDocumentTypes().contains(LegalDocumentType.TERMS_OF_USE.name()));
        assertTrue(status.missingDocumentTypes().contains(LegalDocumentType.PRIVACY_POLICY.name()));
        verifyNoInteractions(consentRecordRepository);
    }
}