package com.conectaedu.api.modules.consent.repository;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    Boolean existsByDocumentTypeAndVersion(LegalDocumentType documentType, String version);

    //Versão em vigor de um tipo, Só existe uma PUBLISHED por tipo.
    Optional<LegalDocument> findByDocumentTypeAndStatus(
            LegalDocumentType documentType, LegalDocumentStatus status);

    //Todas as versões em vigor, usado para montar as pendências do usuário.
    List<LegalDocument> findByStatus(LegalDocumentStatus status);
}
