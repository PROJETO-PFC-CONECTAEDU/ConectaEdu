package com.conectaedu.api.modules.consent.repository;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//H2 de teste, sem mock, O que interessa aqui e se as consultas derivadas
//realmente filtram por tipo e situacao do jeito que o service espera.

@DataJpaTest
@ActiveProfiles("test")
class LegalDocumentRepositoryIntegrationTest {

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    private LegalDocument documento(LegalDocumentType tipo, String versao, LegalDocumentStatus status) {
        LegalDocument documento = new LegalDocument();
        documento.setDocumentType(tipo);
        documento.setVersion(versao);
        documento.setTitle("Documento " + versao);
        documento.setContent("Conteúdo da versão " + versao);
        documento.setContentHash("hash-" + versao);
        documento.setStatus(status);
        documento.setPublishedAt(LocalDateTime.now());
        documento.setPublishedBy(UUID.randomUUID());
        return documento;
    }

    //A consulta por tipo e situacao devolve apenas a versao em vigor.
    @Test
    void deveRecuperarApenasAVersaoEmVigorDoTipo() {
        // preparacao
        legalDocumentRepository.save(
                documento(LegalDocumentType.TERMS_OF_USE, "1.0", LegalDocumentStatus.ARCHIVED));
        legalDocumentRepository.save(
                documento(LegalDocumentType.TERMS_OF_USE, "2.0", LegalDocumentStatus.PUBLISHED));
        legalDocumentRepository.save(
                documento(LegalDocumentType.PRIVACY_POLICY, "1.0", LegalDocumentStatus.PUBLISHED));

        // execucao
        Optional<LegalDocument> emVigor = legalDocumentRepository.findByDocumentTypeAndStatus(
                LegalDocumentType.TERMS_OF_USE, LegalDocumentStatus.PUBLISHED);

        // verificacao
        assertTrue(emVigor.isPresent());
        assertEquals("2.0", emVigor.get().getVersion());
        assertEquals(2, legalDocumentRepository.findByStatus(LegalDocumentStatus.PUBLISHED).size());
    }

    //Termo 1.0 e politica 1.0 convivem sem conflito, a numeracao de cada tipo corre separada.

    @Test
    void deveIdentificarVersaoDuplicadaApenasDentroDoMesmoTipo() {
        // preparacao
        legalDocumentRepository.save(
                documento(LegalDocumentType.TERMS_OF_USE, "1.0", LegalDocumentStatus.PUBLISHED));

        // execucao
        boolean mesmoTipoMesmaVersao = legalDocumentRepository.existsByDocumentTypeAndVersion(
                LegalDocumentType.TERMS_OF_USE, "1.0");
        boolean outroTipoMesmaVersao = legalDocumentRepository.existsByDocumentTypeAndVersion(
                LegalDocumentType.PRIVACY_POLICY, "1.0");
        boolean mesmoTipoOutraVersao = legalDocumentRepository.existsByDocumentTypeAndVersion(
                LegalDocumentType.TERMS_OF_USE, "2.0");

        // verificacao
        assertTrue(mesmoTipoMesmaVersao);
        assertFalse(outroTipoMesmaVersao);
        assertFalse(mesmoTipoOutraVersao);
    }
}
