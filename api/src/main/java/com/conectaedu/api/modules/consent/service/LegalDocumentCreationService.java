package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.request.LegalDocumentCreationRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentCreationResponseDTO;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.InvalidDocumentVersionException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class LegalDocumentCreationService {

    private final LegalDocumentRepository legalDocumentRepository;
    private final UserRepository userRepository;

    //Cria e publica a nova versão, arquivando a anterior do mesmo tipo.
    @Transactional
    public LegalDocumentCreationResponseDTO createDocument(LegalDocumentCreationRequestDTO request) {

        requirePlatformAdmin(request);

        //Versão é única por tipo, não existem dois "Termos de Uso 1.0".
        if (legalDocumentRepository.existsByDocumentTypeAndVersion(
                request.documentType(), request.version())) {
            throw new InvalidDocumentVersionException(
                    "Já existe a versão " + request.version() + " para " + request.documentType());
        }

        //Só existe uma versão em vigor por tipo, a anterior é arquivada.
        legalDocumentRepository
                .findByDocumentTypeAndStatus(request.documentType(), LegalDocumentStatus.PUBLISHED)
                .ifPresent(previous -> {
                    requireNewerVersion(request.version(), previous.getVersion());

                    previous.setStatus(LegalDocumentStatus.ARCHIVED);
                    previous.setArchivedAt(LocalDateTime.now());
                    legalDocumentRepository.save(previous);
                });

        LegalDocument document = new LegalDocument();
        document.setDocumentType(request.documentType());
        document.setVersion(request.version());
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setChangeSummary(request.changeSummary());
        document.setPublishedBy(request.publisherUserId());
        document.setStatus(LegalDocumentStatus.PUBLISHED);

        //prova que o usuario aceitou (Hash congela o texto publicado)
        document.setContentHash(sha256(request.content()));

        legalDocumentRepository.save(document);

        return new LegalDocumentCreationResponseDTO(
                document.getId(),
                document.getVersion(),
                "Documento publicado. Os usuários precisarão aceitar esta versão.");
    }

    //Publicar uma versão anterior arquivaria a atual.

    private void requireNewerVersion(String version, String currentVersion) {
        if (versionNumber(version) <= versionNumber(currentVersion)) {
            throw new InvalidDocumentVersionException(
                    "A versão " + version + " não é posterior à versão em vigor " + currentVersion);
        }
    }

    //O DTO limita cada em  três dígitos.

    private int versionNumber(String version) {
        String[] parts = version.split("\\.");
        return Integer.parseInt(parts[0]) * 1000 + Integer.parseInt(parts[1]);
    }

    private void requirePlatformAdmin(LegalDocumentCreationRequestDTO request) {
        User publisher = userRepository.findById(request.publisherUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuário publicador não encontrado!"));

        if (publisher.getUserRole() != UserRole.PLATFORM_ADMIN) {
            throw new IllegalArgumentException(
                    "Somente o administrador da plataforma pode publicar documentos legais.");
        }
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", ex);
        }
    }
}