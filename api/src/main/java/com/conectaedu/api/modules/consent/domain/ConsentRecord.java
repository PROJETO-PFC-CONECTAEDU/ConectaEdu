package com.conectaedu.api.modules.consent.domain;

import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.shared.enums.ConsentAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_records")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    //Aponta para a versão exata aceita, nunca para "o documento atual".
    @ManyToOne(optional = false)
    @JoinColumn(name = "legal_document_id", nullable = false, updatable = false)
    private LegalDocument legalDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsentAction action = ConsentAction.ACCEPTED;

    @Column(name = "accepted_at", nullable = false, updatable = false)
    private LocalDateTime acceptedAt = LocalDateTime.now();

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    //Cópia do hash no momento do aceite: prova qual texto foi aceito.
    @Column(name = "accepted_content_hash")
    private String acceptedContentHash;
}
