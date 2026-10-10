package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.ConsentRecord;
import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.request.ConsentAcceptanceRequestDTO;
import com.conectaedu.api.modules.consent.dto.request.ConsentWithdrawalRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.enums.ConsentAction;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import com.conectaedu.api.shared.exceptions.ConsentAlreadyGivenException;
import com.conectaedu.api.shared.exceptions.ConsentNotFoundException;
import com.conectaedu.api.shared.exceptions.InvalidLegalDocumentStatusException;
import com.conectaedu.api.shared.exceptions.LegalDocumentNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Regras, somente a versao em vigor pode ser aceita, o mesmo usuario nao
//aceita duas vezes a mesma versao, a revogacao nao apaga o aceite anterior.

@ExtendWith(MockitoExtension.class)
class ConsentAcceptanceServiceTest {

    private static final String HASH = "7d3a1f9c0b5e4a2d8c6f1b0a9e8d7c6b5a4938271605f4e3d2c1b0a9f8e7d6c5";

    @Mock
    private ConsentRecordRepository consentRecordRepository;

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConsentAcceptanceService consentAcceptanceService;

    private User usuario(UUID id) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setName("Érico Bruno Marin");
        return usuario;
    }

    private LegalDocument documento(UUID id, LegalDocumentStatus status) {
        LegalDocument documento = new LegalDocument();
        documento.setId(id);
        documento.setDocumentType(LegalDocumentType.TERMS_OF_USE);
        documento.setVersion("1.0");
        documento.setStatus(status);
        documento.setContentHash(HASH);
        return documento;
    }

    //O aceite da versao em vigor copia o hash do texto aceito.
    @Test
    void deveRegistrarAceiteCopiandoOHashDoDocumento() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(legalDocumentRepository.findById(documentoId))
                .thenReturn(Optional.of(documento(documentoId, LegalDocumentStatus.PUBLISHED)));
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, documentoId, ConsentAction.ACCEPTED)).thenReturn(false);
        ArgumentCaptor<ConsentRecord> capturado = ArgumentCaptor.forClass(ConsentRecord.class);

        // execucao
        ConsentRecordResponseDTO resposta = consentAcceptanceService.acceptDocument(
                new ConsentAcceptanceRequestDTO(usuarioId, documentoId));

        // verificacao
        verify(consentRecordRepository).save(capturado.capture());
        ConsentRecord registro = capturado.getValue();
        assertEquals(ConsentAction.ACCEPTED, registro.getAction());
        assertEquals(HASH, registro.getAcceptedContentHash());
        assertNotNull(registro.getAcceptedAt());
        assertEquals(ConsentAction.ACCEPTED.name(), resposta.action());
        assertEquals("1.0", resposta.documentVersion());
    }

    //Versao arquivada foi substituida e nao pode mais ser aceita.
    @Test
    void deveLancarExcecaoQuandoDocumentoNaoEstaEmVigor() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(legalDocumentRepository.findById(documentoId))
                .thenReturn(Optional.of(documento(documentoId, LegalDocumentStatus.ARCHIVED)));

        // execucao
        InvalidLegalDocumentStatusException erro = assertThrows(InvalidLegalDocumentStatusException.class,
                () -> consentAcceptanceService.acceptDocument(
                        new ConsentAcceptanceRequestDTO(usuarioId, documentoId)));

        // verificacao
        assertEquals("A versão 1.0 foi substituída. Aceite a versão em vigor.", erro.getMessage());
        verify(consentRecordRepository, never()).save(any(ConsentRecord.class));
    }

    //Aceitar duas vezes a mesma versao.
    @Test
    void deveLancarExcecaoQuandoUsuarioJaAceitouAVersao() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(legalDocumentRepository.findById(documentoId))
                .thenReturn(Optional.of(documento(documentoId, LegalDocumentStatus.PUBLISHED)));
        when(consentRecordRepository.existsByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, documentoId, ConsentAction.ACCEPTED)).thenReturn(true);

        // execucao
        ConsentAlreadyGivenException erro = assertThrows(ConsentAlreadyGivenException.class,
                () -> consentAcceptanceService.acceptDocument(
                        new ConsentAcceptanceRequestDTO(usuarioId, documentoId)));

        // verificacao
        assertEquals("Este usuário já aceitou a versão 1.0", erro.getMessage());
        verify(consentRecordRepository, never()).save(any(ConsentRecord.class));
    }

    //Documento inexistente.
    @Test
    void deveLancarExcecaoQuandoDocumentoNaoExiste() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(userRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(legalDocumentRepository.findById(documentoId)).thenReturn(Optional.empty());

        // execucao
        LegalDocumentNotFoundException erro = assertThrows(LegalDocumentNotFoundException.class,
                () -> consentAcceptanceService.acceptDocument(
                        new ConsentAcceptanceRequestDTO(usuarioId, documentoId)));

        // verificacao
        assertEquals("Documento não encontrado!", erro.getMessage());
        verify(consentRecordRepository, never()).save(any(ConsentRecord.class));
    }

    // Titular inexistente.
    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        when(userRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // execucao
        UserNotFoundException erro = assertThrows(UserNotFoundException.class,
                () -> consentAcceptanceService.acceptDocument(
                        new ConsentAcceptanceRequestDTO(usuarioId, UUID.randomUUID())));

        // verificacao
        assertEquals("Usuário não encontrado!", erro.getMessage());
        verify(consentRecordRepository, never()).save(any(ConsentRecord.class));
    }

    //O aceite nao e apagado, so recebe a data de fim.
    //A prova do consentimento anterior continua intacta, como exige a LGPD.
    @Test
    void deveRevogarPreservandoORegistroDoAceiteAnterior() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        LocalDateTime aceiteOriginal = LocalDateTime.now().minusDays(30);

        ConsentRecord registro = new ConsentRecord();
        registro.setId(UUID.randomUUID());
        registro.setUser(usuario(usuarioId));
        registro.setLegalDocument(documento(documentoId, LegalDocumentStatus.PUBLISHED));
        registro.setAction(ConsentAction.ACCEPTED);
        registro.setAcceptedAt(aceiteOriginal);
        registro.setAcceptedContentHash(HASH);

        when(consentRecordRepository.findByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, documentoId, ConsentAction.ACCEPTED)).thenReturn(Optional.of(registro));

        // execucao
        ConsentRecordResponseDTO resposta = consentAcceptanceService.withdrawConsent(
                new ConsentWithdrawalRequestDTO(usuarioId, documentoId));

        // verificacao
        assertEquals(ConsentAction.WITHDRAWN.name(), resposta.action());
        assertNotNull(resposta.withdrawnAt());
        assertEquals(aceiteOriginal, resposta.acceptedAt());
        assertEquals(HASH, resposta.acceptedContentHash());
        verify(consentRecordRepository).save(registro);
    }

    //Violacao, nao se revoga o que nunca foi aceito.
    @Test
    void deveLancarExcecaoAoRevogarSemAceiteVigente() {
        // preparacao
        UUID usuarioId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        when(consentRecordRepository.findByUser_IdAndLegalDocument_IdAndAction(
                usuarioId, documentoId, ConsentAction.ACCEPTED)).thenReturn(Optional.empty());

        // execucao
        ConsentNotFoundException erro = assertThrows(ConsentNotFoundException.class,
                () -> consentAcceptanceService.withdrawConsent(
                        new ConsentWithdrawalRequestDTO(usuarioId, documentoId)));

        // verificacao
        assertEquals("Não há aceite vigente deste documento para este usuário.", erro.getMessage());
        verify(consentRecordRepository, never()).save(any(ConsentRecord.class));
    }
}
