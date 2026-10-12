package com.conectaedu.api.modules.consent.domain;

import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "legal_documents")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, updatable = false, length = 20)
    private LegalDocumentType documentType;

    //Identifica a versão do documento. Única por tipo.
    @Column(nullable = false, length = 20, updatable = false)
    private String version;

    @Column(nullable = false, length = 200)
    private String title;

    //Texto publicado é imutável para mudar, cria-se uma nova versão.
    //Aumentado tamanho da String para conseguir exibir o conteúdo completo.
    @Column(nullable = false, updatable = false, length = 20000)
    private String content;

    //Impressão digital do texto. Prova que o conteúdo não mudou depois.
    @Column(name = "content_hash", nullable = false, updatable = false)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LegalDocumentStatus status = LegalDocumentStatus.PUBLISHED;

    //Resumo do que mudou em relação à versão anterior, exibido ao usuário.
    @Column(name = "change_summary", length = 1000)
    private String changeSummary;

    @Column(name = "published_at", nullable = false, updatable = false)
    private LocalDateTime publishedAt = LocalDateTime.now();

    @Column(name = "published_by", nullable = false, updatable = false)
    private UUID publishedBy;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
}