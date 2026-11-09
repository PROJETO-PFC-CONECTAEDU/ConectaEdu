package com.conectaedu.api.modules.university.controller;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Validação integrada entre Controller, Facade, Service e banco H2.
// Cada teste possui cenário independente e a base é limpada antes da execução.

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UniversityIntegrationTest {

    private static final String CNPJ = "12345671000112";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UniversityRepository universityRepository;

    @BeforeEach
    void limparBase() {
        universityRepository.deleteAll();
    }

    private String corpoDeCadastro(String cnpj) {
        return """
                {
                  "name": "Universidade ABCD",
                  "cnpj": "%s",
                  "coordinator": "Joao P",
                  "address": "R. teste, 200"
                }
                """.formatted(cnpj);
    }

    //Caminho "cenario feliz" da API 201, corpo com id e mensagem, e registro persistido no banco.
    @Test
    @WithMockUser(roles = "PLATFORM_ADMIN")
    void devePersistirUniversidadeComoPendenteERetornar201() throws Exception {
        mockMvc.perform(post("/api/v1/universities")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CNPJ)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.message").value("Universidade criada com sucesso!"));

        University persistida = universityRepository.findByCnpj(CNPJ).orElseThrow();
        assertEquals("Universidade ABCD", persistida.getName());
        assertEquals(UniversityStatus.PENDING, persistida.getStatus());
        assertFalse(persistida.isActive());
    }

    //Cenario de erro, CNPJ fora do formato exigido.

    @Test
    @WithMockUser(roles = "PLATFORM_ADMIN")
    void deveRetornar400ENaoPersistirQuandoCnpjForInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/universities")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro("123")))
                .andExpect(status().isBadRequest());

        assertTrue(universityRepository.findByCnpj("123").isEmpty());
        assertEquals(0, universityRepository.count());
    }

    //Caminho cruzando API, service e banco:
    // cadastro, validacao feita pelo ADM e consulta de resultado.

    @Test
    @WithMockUser(roles = "PLATFORM_ADMIN")
    void deveCadastrarValidarEConsultarUniversidadeAprovada() throws Exception {
        MvcResult criacao = mockMvc.perform(post("/api/v1/universities")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CNPJ)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode corpoCriacao = objectMapper.readTree(criacao.getResponse().getContentAsString());
        UUID id = UUID.fromString(corpoCriacao.get("id").asText());

        mockMvc.perform(patch("/api/v1/universities/{id}/validate", id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validationNotes\":\"Documentação conferida pelo ADM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/universities/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canReceiveStudents").value(true))
                .andExpect(jsonPath("$.validatedAt").exists())
                .andExpect(jsonPath("$.validationNotes").value("Documentação conferida pelo ADM"));

        University persistida = universityRepository.findById(id).orElseThrow();
        assertEquals(UniversityStatus.APPROVED, persistida.getStatus());
        assertTrue(persistida.isActive());
    }

    //Cenario de recusa pela API, a situacao muda para REJECTED e a universidade segue inativa.

    @Test
    @WithMockUser(roles = "PLATFORM_ADMIN")
    void deveRecusarUniversidadeEManterInativa() throws Exception {
        MvcResult criacao = mockMvc.perform(post("/api/v1/universities")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CNPJ)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode corpoCriacao = objectMapper.readTree(criacao.getResponse().getContentAsString());
        UUID id = UUID.fromString(corpoCriacao.get("id").asText());

        mockMvc.perform(patch("/api/v1/universities/{id}/reject", id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validationNotes\":\"CNPJ inválido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.active").value(false));

        University persistida = universityRepository.findById(id).orElseThrow();
        assertEquals(UniversityStatus.REJECTED, persistida.getStatus());
        assertFalse(persistida.isActive());
    }
}
