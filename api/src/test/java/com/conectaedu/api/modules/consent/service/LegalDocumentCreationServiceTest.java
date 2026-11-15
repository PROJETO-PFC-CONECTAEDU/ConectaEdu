package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.domain.LegalDocument;
import com.conectaedu.api.modules.consent.dto.request.LegalDocumentCreationRequestDTO;
import com.conectaedu.api.modules.consent.dto.response.LegalDocumentCreationResponseDTO;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.enums.LegalDocumentStatus;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.exceptions.InvalidDocumentVersionException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Somente o PLATFORM_ADMIN publica documentos legais, a versao e unica
//por tipo a nova versao precisa ser posterior a que esta em vigor o hash SHA-256
//congela o texto publicado.

@ExtendWith(MockitoExtension.class)
class LegalDocumentCreationServiceTest {

    private static final String CONTEUDO = "Termos de Uso da plataforma ConectaEDU. Versao usada nos testes.";

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LegalDocumentCreationService legalDocumentCreationService;

    private User administrador(UUID id) {
        User usuario = new User();
        usuario.setId(id);
        usuario.setName("João P.");
        usuario.setUserRole(UserRole.PLATFORM_ADMIN);
        return usuario;
    }

    private LegalDocumentCreationRequestDTO request(String versao, UUID publicador) {
        return new LegalDocumentCreationRequestDTO(
                LegalDocumentType.TERMS_OF_USE, versao, "Termos de Uso",
                CONTEUDO, "Publicação inicial", publicador);
    }

    private LegalDocument vigente(String versao) {
        LegalDocument documento = new LegalDocument();
        documento.setId(UUID.randomUUID());
        documento.setDocumentType(LegalDocumentType.TERMS_OF_USE);
        documento.setVersion(versao);
        documento.setStatus(LegalDocumentStatus.PUBLISHED);
        return documento;
    }

    //O hash esperado e calculado aqui, no proprio teste, e nao copiado do servico.
    private String sha256Esperado(String texto) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
    }

    //A primeira versao publicada ja nasce com o hash do conteudo.
    @Test
    void devePublicarPrimeiraVersaoComHashDoConteudo() throws NoSuchAlgorithmException {
        // preparacao
        UUID publicador = UUID.randomUUID();
        when(userRepository.findById(publicador)).thenReturn(Optional.of(administrador(publicador)));
        when(legalDocumentRepository.existsByDocumentTypeAndVersion(LegalDocumentType.TERMS_OF_USE, "1.0"))
                .thenReturn(false);
        when(legalDocumentRepository.findByDocumentTypeAndStatus(
                LegalDocumentType.TERMS_OF_USE, LegalDocumentStatus.PUBLISHED))
                .thenReturn(Optional.empty());
        ArgumentCaptor<LegalDocument> capturado = ArgumentCaptor.forClass(LegalDocument.class);

        // execucao
        LegalDocumentCreationResponseDTO resposta =
                legalDocumentCreationService.createDocument(request("1.0", publicador));

        // verificacao
        verify(legalDocumentRepository).save(capturado.capture());
        LegalDocument salvo = capturado.getValue();
        assertEquals(LegalDocumentStatus.PUBLISHED, salvo.getStatus());
        assertEquals(sha256Esperado(CONTEUDO), salvo.getContentHash());
        assertEquals(publicador, salvo.getPublishedBy());
        assertEquals("1.0", resposta.version());
    }

    // Publicar a 2.0 arquiva a 1.0 do mesmo tipo, garantindo uma unica versao em vigor.
    @Test
    void deveArquivarVersaoAnteriorAoPublicarVersaoPosterior() {
        // preparacao
        UUID publicador = UUID.randomUUID();
        LegalDocument anterior = vigente("1.0");
        when(userRepository.findById(publicador)).thenReturn(Optional.of(administrador(publicador)));
        when(legalDocumentRepository.existsByDocumentTypeAndVersion(LegalDocumentType.TERMS_OF_USE, "2.0"))
                .thenReturn(false);
        when(legalDocumentRepository.findByDocumentTypeAndStatus(
                LegalDocumentType.TERMS_OF_USE, LegalDocumentStatus.PUBLISHED))
                .thenReturn(Optional.of(anterior));

        // execucao
        legalDocumentCreationService.createDocument(request("2.0", publicador));

        // verificacao
        assertEquals(LegalDocumentStatus.ARCHIVED, anterior.getStatus());
        assertNotNull(anterior.getArchivedAt());
        verify(legalDocumentRepository, times(2)).save(any(LegalDocument.class));
    }

    //Nao existem dois "Termos de Uso 1.0".
    @Test
    void deveLancarExcecaoQuandoVersaoJaExisteParaOTipo() {
        // preparacao
        UUID publicador = UUID.randomUUID();
        when(userRepository.findById(publicador)).thenReturn(Optional.of(administrador(publicador)));
        when(legalDocumentRepository.existsByDocumentTypeAndVersion(LegalDocumentType.TERMS_OF_USE, "1.0"))
                .thenReturn(true);

        // execucao
        InvalidDocumentVersionException erro = assertThrows(InvalidDocumentVersionException.class,
                () -> legalDocumentCreationService.createDocument(request("1.0", publicador)));

        // verificacao
        assertEquals("Já existe a versão 1.0 para TERMS_OF_USE", erro.getMessage());
        verify(legalDocumentRepository, never()).save(any(LegalDocument.class));
    }

    //Publicador sem o perfil de administrador da plataforma.
    @Test
    void deveLancarExcecaoQuandoPublicadorNaoEAdministradorDaPlataforma() {
        // preparacao
        UUID publicador = UUID.randomUUID();
        User estudante = administrador(publicador);
        estudante.setUserRole(UserRole.STUDENT);
        when(userRepository.findById(publicador)).thenReturn(Optional.of(estudante));

        // execucao
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> legalDocumentCreationService.createDocument(request("1.0", publicador)));

        // verificacao
        assertEquals("Somente o administrador da plataforma pode publicar documentos legais.", erro.getMessage());
        verify(legalDocumentRepository, never()).save(any(LegalDocument.class));
    }

    //Publicador inexistente.
    @Test
    void deveLancarExcecaoQuandoPublicadorNaoExiste() {
        // preparacao
        UUID publicador = UUID.randomUUID();
        when(userRepository.findById(publicador)).thenReturn(Optional.empty());

        // execucao
        UserNotFoundException erro = assertThrows(UserNotFoundException.class,
                () -> legalDocumentCreationService.createDocument(request("1.0", publicador)));

        // verificacao
        assertEquals("Usuário publicador não encontrado!", erro.getMessage());
        verify(legalDocumentRepository, never()).save(any(LegalDocument.class));
    }

    //Com a 1.2 publicada, republicar a propria 1.2 tambem e barrado, a comparacao e <=, nao <.
    @ParameterizedTest
    @ValueSource(strings = {"0.9", "1.1", "1.2"})
    void deveRecusarVersaoQueNaoSuperaAVigente(String versaoTentada) {
        // preparacao
        UUID publicador = UUID.randomUUID();
        when(userRepository.findById(publicador)).thenReturn(Optional.of(administrador(publicador)));
        when(legalDocumentRepository.existsByDocumentTypeAndVersion(LegalDocumentType.TERMS_OF_USE, versaoTentada))
                .thenReturn(false);
        when(legalDocumentRepository.findByDocumentTypeAndStatus(
                LegalDocumentType.TERMS_OF_USE, LegalDocumentStatus.PUBLISHED))
                .thenReturn(Optional.of(vigente("1.2")));

        // execucao
        InvalidDocumentVersionException erro = assertThrows(InvalidDocumentVersionException.class,
                () -> legalDocumentCreationService.createDocument(request(versaoTentada, publicador)));

        // verificacao
        assertTrue(erro.getMessage().contains("não é posterior à versão em vigor 1.2"));
        verify(legalDocumentRepository, never()).save(any(LegalDocument.class));
    }

    //Ordenado como texto, "1.10" cai antes de "1.9", mas e a versao mais nova.
    @Test
    void deveAceitarVersao110ComoPosteriorA19() {
        // preparacao
        UUID publicador = UUID.randomUUID();
        when(userRepository.findById(publicador)).thenReturn(Optional.of(administrador(publicador)));
        when(legalDocumentRepository.existsByDocumentTypeAndVersion(LegalDocumentType.TERMS_OF_USE, "1.10"))
                .thenReturn(false);
        when(legalDocumentRepository.findByDocumentTypeAndStatus(
                LegalDocumentType.TERMS_OF_USE, LegalDocumentStatus.PUBLISHED))
                .thenReturn(Optional.of(vigente("1.9")));

        // execucao
        LegalDocumentCreationResponseDTO resposta =
                legalDocumentCreationService.createDocument(request("1.10", publicador));

        // verificacao
        assertEquals("1.10", resposta.version());
        verify(legalDocumentRepository, times(2)).save(any(LegalDocument.class));
    }
}