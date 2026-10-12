package com.conectaedu.api.modules.consent.repository;

import com.conectaedu.api.modules.consent.domain.ConsentRecord;
import com.conectaedu.api.shared.enums.ConsentAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    //Histórico do titular.
    List<ConsentRecord> findByUser_IdOrderByAcceptedAtDesc(UUID userId);

    //Aceite vigente de uma versão, usado na revogação.
    Optional<ConsentRecord> findByUser_IdAndLegalDocument_IdAndAction(
            UUID userId, UUID legalDocumentId, ConsentAction action);

    //Usado para saber se o usuário já aceitou a versão em vigor.
    Boolean existsByUser_IdAndLegalDocument_IdAndAction(
            UUID userId, UUID legalDocumentId, ConsentAction action);
}
