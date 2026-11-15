package com.conectaedu.api.modules.consent.controller;

import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import com.conectaedu.api.modules.consent.repository.LegalDocumentRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.shared.enums.ConsentAction;
import com.conectaedu.api.shared.enums.LegalDocumentType;
import com.conectaedu.api.shared.enums.UserType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Integracao  modulo consent, publicacao do documento, aceite do titular
//e consulta de pendencias, usando banco H2.

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsentIntegrationTest {

    //Admin autenticado. Com a sessao STATELESS, o @WithMockUser nao chega na requisicao,
    //o post-processor injeta o contexto direto.
    private static final RequestPostProcessor ADMIN =
            SecurityMockMvcRequestPostProcessors.user("admin").roles("PLATFORM_ADMIN");

    private static final String CONTEUDO = "Termos de Uso da plataforma ConectaEDU. Texto usado na integração.";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    @Autowired
    private ConsentRecordRepository consentRecordRepository;

    private UUID administradorId;

    @BeforeEach
    void prepararCenario() {
        consentRecordRepository.deleteAll();
        legalDocumentRepository.deleteAll();
        userRepository.deleteAll();

        User administrador = PlatformAdmin.builder()
                .name("João P.")
                .email("admin.teste@conectaedu.com")
                .password("senha-de-teste")
                .userType(UserType.PERSON)
                .createdAt(LocalDateTime.now())
                .build();

        administradorId = userRepository.save(administrador).getId();
    }

    private String corpoDePublicacao(String versao) {
        return """
                {
                  "documentType": "TERMS_OF_USE",
                  "version": "%s",
                  "title": "Termos de Uso",
                  "content": "%s",
                  "changeSummary": "Publicação inicial",
                  "publisherUserId": "%s"
                }
                """.formatted(versao, CONTEUDO, administradorId);
    }


    //aceitar como titular e confirmar que a pendencia deixou de existir.
    @Test
    void devePublicarAceitarEZerarAPendenciaDoUsuario() throws Exception {
        MvcResult publicacao = mockMvc.perform(post("/api/v1/legal-documents")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDePublicacao("1.0")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andReturn();

        JsonNode corpoPublicacao = objectMapper.readTree(publicacao.getResponse().getContentAsString());
        UUID documentoId = UUID.fromString(corpoPublicacao.get("id").asText());

        mockMvc.perform(get("/api/v1/consents/status/{userId}", administradorId).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsConsent").value(true))
                .andExpect(jsonPath("$.pendingDocuments.length()").value(1));

        mockMvc.perform(post("/api/v1/consents")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s","legalDocumentId":"%s"}
                                """.formatted(administradorId, documentoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("ACCEPTED"))
                .andExpect(jsonPath("$.acceptedContentHash").isNotEmpty());

        mockMvc.perform(get("/api/v1/consents/status/{userId}", administradorId).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needsConsent").value(false))
                .andExpect(jsonPath("$.pendingDocuments").isEmpty())
                .andExpect(jsonPath("$.missingDocumentTypes[0]")
                        .value(LegalDocumentType.PRIVACY_POLICY.name()));

        assertEquals(1, consentRecordRepository.count());
        assertEquals(ConsentAction.ACCEPTED,
                consentRecordRepository.findAll().get(0).getAction());
    }

    //Cenario de erro, aceitar documento inexistente devolve 404 com corpo de erro

    @Test
    void deveRetornar404ComCorpoDeErroAoAceitarDocumentoInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/consents")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s","legalDocumentId":"%s"}
                                """.formatted(administradorId, UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Documento não encontrado!"))
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, consentRecordRepository.count());
    }

    //Cenario de erro de validacao, campos obrigatorios ausentes devolvem 400

    @Test
    void deveRetornar400ComCamposReprovadosAoPublicarSemVersao() throws Exception {
        mockMvc.perform(post("/api/v1/legal-documents")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documentType": "TERMS_OF_USE",
                                  "title": "Termos de Uso",
                                  "content": "%s",
                                  "publisherUserId": "%s"
                                }
                                """.formatted(CONTEUDO, administradorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Requisição com campos inválidos"))
                .andExpect(jsonPath("$.campos.version").exists());

        assertEquals(0, legalDocumentRepository.count());
    }

    //O front exibe o termo antes do login.
    @Test
    void deveExporVersaoEmVigorSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/v1/legal-documents")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDePublicacao("1.0")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/legal-documents/current/{tipo}", LegalDocumentType.TERMS_OF_USE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.contentHash").isNotEmpty());
    }
}