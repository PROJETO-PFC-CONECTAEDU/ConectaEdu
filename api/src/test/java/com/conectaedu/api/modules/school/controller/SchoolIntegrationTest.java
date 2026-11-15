package com.conectaedu.api.modules.school.controller;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.enums.SchoolStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchoolIntegrationTest {

    private static final RequestPostProcessor ADMIN =
            SecurityMockMvcRequestPostProcessors.user("admin").roles("PLATFORM_ADMIN");

    //diretor de escola autenticado, sem permissao para cadastrar escolas.
    private static final RequestPostProcessor DIRETOR =
            SecurityMockMvcRequestPostProcessors.user("diretor").roles("SCHOOL_DIRECTOR");

    private static final String CIE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SchoolRepository schoolRepository;

    @BeforeEach
    void limparBase() {
        schoolRepository.deleteAll();
    }

    private String corpoDeCadastro(String cie) {
        return """
                {
                  "name": "Escola Estadual ABCD",
                  "cie": "%s",
                  "director": "Maria Sousa",
                  "address": "R. teste, 100",
                  "latitude": -23.5505,
                  "longitude": -46.6333
                }
                """.formatted(cie);
    }

    //A resposta do cadastro nao devolve o id, ele e buscado no banco pelo CIE.
    private UUID cadastrarEscola() throws Exception {
        mockMvc.perform(post("/api/v1/schools")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CIE)))
                .andExpect(status().isCreated());

        return schoolRepository.findByCie(CIE).orElseThrow().getId();
    }

    //caminho feliz.
    @Test
    void devePersistirEscolaComoPendenteERetornar201() throws Exception {
        mockMvc.perform(post("/api/v1/schools")
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CIE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Escola cadastrada com sucesso!"));

        School persistida = schoolRepository.findByCie(CIE).orElseThrow();
        assertEquals("Escola Estadual ABCD", persistida.getName());
        assertEquals("Maria Sousa", persistida.getDirector());
        assertEquals(SchoolStatus.PENDING_ACTIVATION, persistida.getStatus());
        assertFalse(persistida.isActive());
        assertNotNull(persistida.getCreatedAt());
    }

    //cenario de erro no qual somente o administrador da plataforma cadastra escolas.

    @Test
    void deveRetornar403ENaoPersistirQuandoUsuarioNaoEAdministrador() throws Exception {
        mockMvc.perform(post("/api/v1/schools")
                        .with(DIRETOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDeCadastro(CIE)))
                .andExpect(status().isForbidden());

        assertTrue(schoolRepository.findByCie(CIE).isEmpty());
        assertEquals(0, schoolRepository.count());
    }

    //caminho cruzando API, service e banco: cadastro, ativacao feita pelo ADM e consulta de resultado.

    @Test
    void deveCadastrarAtivarEConsultarEscola() throws Exception {
        UUID id = cadastrarEscola();

        mockMvc.perform(patch("/api/v1/schools/{id}/activate", id)
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validationNotes\":\"Documentação conferida pelo ADM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/schools/{id}", id).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cie").value(CIE))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.active").value(true));

        School persistida = schoolRepository.findById(id).orElseThrow();
        assertEquals(SchoolStatus.ACTIVE, persistida.getStatus());
        assertTrue(persistida.isActive());
        assertEquals("Documentação conferida pelo ADM", persistida.getValidationNotes());
    }

    //cenario de inativacao pela API, a situacao muda para INACTIVE e a escola segue inativa.

    @Test
    void deveInativarEscolaPendenteEManterInativa() throws Exception {
        UUID id = cadastrarEscola();

        mockMvc.perform(patch("/api/v1/schools/{id}/deactivate", id)
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"validationNotes\":\"Documentação incompleta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.active").value(false));

        School persistida = schoolRepository.findById(id).orElseThrow();
        assertEquals(SchoolStatus.INACTIVE, persistida.getStatus());
        assertFalse(persistida.isActive());
        assertNotNull(persistida.getValidatedAt());
    }

    //alteracao do cadastro pela API, os dados mudam no banco e o CIE permanece o mesmo.

    @Test
    void deveAtualizarDadosDaEscola() throws Exception {
        UUID id = cadastrarEscola();

        mockMvc.perform(put("/api/v1/schools/{id}", id)
                        .with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Escola Estadual ABCDE",
                                  "director": "Joao Paulo",
                                  "address": "Av. nova, 500"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Escola Estadual ABCDE"))
                .andExpect(jsonPath("$.director").value("Joao Paulo"))
                .andExpect(jsonPath("$.cie").value(CIE));

        School persistida = schoolRepository.findById(id).orElseThrow();
        assertEquals("Escola Estadual ABCDE", persistida.getName());
        assertEquals("Av. nova, 500", persistida.getAddress());
        assertEquals(-23.5505, persistida.getLatitude());
        assertNotNull(persistida.getUpdatedAt());
    }

    //exclusao pela API, 204 sem corpo e registro removido do banco.

    @Test
    void deveExcluirEscolaERetornar204() throws Exception {
        UUID id = cadastrarEscola();

        mockMvc.perform(delete("/api/v1/schools/{id}", id).with(ADMIN))
                .andExpect(status().isNoContent());

        assertTrue(schoolRepository.findById(id).isEmpty());
        assertEquals(0, schoolRepository.count());
    }
}
