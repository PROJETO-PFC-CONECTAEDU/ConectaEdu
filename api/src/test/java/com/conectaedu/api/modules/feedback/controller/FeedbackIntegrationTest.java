package com.conectaedu.api.modules.feedback.controller;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//o diretor avalia o estudante e o estudante avalia a sala de uma necessidade concluida.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolDirectorRepository schoolDirectorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private StudentEvaluationRepository studentEvaluationRepository;

    @Autowired
    private ClassEvaluationRepository classEvaluationRepository;

    private School escola;
    private SchoolDirector diretor;
    private Student estudante;
    private Student outroEstudante;
    private Demand concluida;
    private Demand emAndamento;

    @BeforeEach
    void prepararCenario() {
        escola = new School();
        escola.setName("Escola Estadual ABCD");
        escola.setCie("111111");
        escola.setCreatedAt(LocalDateTime.now());
        escola = schoolRepository.save(escola);

        diretor = new SchoolDirector();
        diretor.setName("Joao P");
        diretor.setEmail("diretor.teste@conectaedu.com");
        diretor.setPassword("senha-de-teste");
        diretor.setUserType(UserType.PERSON);
        diretor.setCreatedAt(LocalDateTime.now());
        diretor.setSchool(escola);
        diretor = schoolDirectorRepository.save(diretor);

        estudante = studentRepository.save(estudante("Estudante Teste", "estudante.teste@conectaedu.com"));
        outroEstudante = studentRepository.save(estudante("Maria Clara", "outra.teste@conectaedu.com"));

        concluida = demandRepository.save(necessidade(DemandStatus.FINISHED));
        emAndamento = demandRepository.save(necessidade(DemandStatus.ONGOING));
    }

    @AfterEach
    void limparCenario() {
        studentEvaluationRepository.deleteAll();
        classEvaluationRepository.deleteAll();
        demandRepository.deleteAllById(List.of(concluida.getId(), emAndamento.getId()));
        studentRepository.deleteAllById(List.of(estudante.getId(), outroEstudante.getId()));
        schoolDirectorRepository.deleteById(diretor.getId());
        schoolRepository.deleteById(escola.getId());
    }

    private Student estudante(String nome, String email) {
        Student novo = new Student();
        novo.setName(nome);
        novo.setEmail(email);
        novo.setPassword("senha-de-teste");
        novo.setUserType(UserType.PERSON);
        novo.setCreatedAt(LocalDateTime.now());
        return novo;
    }

    private Demand necessidade(DemandStatus status) {
        Demand necessidade = new Demand();
        necessidade.setTitle("Reforço de Matemática");
        necessidade.setStatus(status);
        necessidade.setSchool(escola);
        necessidade.setDirector(diretor);
        necessidade.setStudent(estudante);
        necessidade.setCreatedAt(LocalDateTime.now());
        if (status == DemandStatus.FINISHED) {
            necessidade.setFinishedAt(LocalDateTime.now());
        }
        return necessidade;
    }

    private RequestPostProcessor autenticado(UUID id, String papel, UUID escolaId) {
        var permissoes = List.of(new SimpleGrantedAuthority("ROLE_" + papel));
        return SecurityMockMvcRequestPostProcessors.user(
                new AuthenticatedUser(id, papel.toLowerCase() + "@conectaedu.com", "", null, escolaId, permissoes));
    }

    private RequestPostProcessor comoDiretor() {
        return autenticado(diretor.getId(), "SCHOOL_DIRECTOR", escola.getId());
    }

    private RequestPostProcessor comoEstudante(Student quem) {
        return autenticado(quem.getId(), "STUDENT", null);
    }

    private String corpoDaAvaliacaoDoEstudante(UUID necessidadeId, UUID estudanteId, int conhecimento) {
        return """
                {
                  "demandId": "%s",
                  "studentId": "%s",
                  "knowledge": %d,
                  "punctuality": 8,
                  "teaching": 9,
                  "commitment": 7,
                  "empathy": 6,
                  "comments": "Ótimo trabalho com a turma."
                }
                """.formatted(necessidadeId, estudanteId, conhecimento);
    }

    private String corpoDaAvaliacaoDaSala(UUID necessidadeId) {
        return """
                {
                  "demandId": "%s",
                  "engagement": 8,
                  "progress": 6,
                  "infrastructure": 10,
                  "comments": "Turma participativa."
                }
                """.formatted(necessidadeId);
    }

    //caminho feliz, com 201.
    @Test
    void devePersistirAvaliacaoDoEstudanteERetornar201ComAMedia() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), estudante.getId(), 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.demandId").value(concluida.getId().toString()))
                .andExpect(jsonPath("$.studentName").value("Estudante Teste"))
                .andExpect(jsonPath("$.evaluatedBy").value(diretor.getId().toString()))
                .andExpect(jsonPath("$.average").value(8.0));

        assertEquals(1, studentEvaluationRepository.count());
        assertTrue(studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                concluida.getId(), estudante.getId(), diretor.getId()));
    }

    //reenviar a avaliacao viraria edicao.
    @Test
    void deveRetornar409AoAvaliarOMesmoEstudanteDuasVezes() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), estudante.getId(), 10)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), estudante.getId(), 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Este estudante já foi avaliado nesta Necessidade."))
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, studentEvaluationRepository.count());
    }

    @Test
    void deveRetornar409AoAvaliarNecessidadeAindaNaoConcluida() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(emAndamento.getId(), estudante.getId(), 10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "A Necessidade está com status ONGOING e só pode ser avaliada depois de concluída."));

        assertEquals(0, studentEvaluationRepository.count());
    }

    //nota fora de 0 a 10 devolve 400 com o campo reprovado.
    @Test
    void deveRetornar400ComCampoReprovadoQuandoNotaPassaDeDez() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), estudante.getId(), 11)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Requisição com campos inválidos"))
                .andExpect(jsonPath("$.campos.knowledge").exists());

        assertEquals(0, studentEvaluationRepository.count());
    }

    @Test
    void deveRetornar400AoAvaliarEstudanteQueNaoAtendeuANecessidade() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), outroEstudante.getId(), 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O estudante informado não atendeu esta Necessidade."));

        assertEquals(0, studentEvaluationRepository.count());
    }

    @Test
    void deveRetornar404AoAvaliarNecessidadeInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoDiretor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(UUID.randomUUID(), estudante.getId(), 10)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Necessidade não encontrada!"))
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, studentEvaluationRepository.count());
    }

    @Test
    void deveRetornar403QuandoEstudanteTentaAvaliarEstudante() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/student")
                        .with(comoEstudante(estudante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDoEstudante(concluida.getId(), estudante.getId(), 10)))
                .andExpect(status().isForbidden());

        assertEquals(0, studentEvaluationRepository.count());
    }

    //caminho feliz da avaliacao da sala feita pelo estudante que atendeu a necessidade.
    @Test
    void devePersistirAvaliacaoDaSalaERetornar201() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/class")
                        .with(comoEstudante(estudante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDaSala(concluida.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.studentId").value(estudante.getId().toString()))
                .andExpect(jsonPath("$.engagement").value(8))
                .andExpect(jsonPath("$.progress").value(6))
                .andExpect(jsonPath("$.infrastructure").value(10));

        assertEquals(1, classEvaluationRepository.count());
        assertTrue(classEvaluationRepository.existsByDemand_IdAndStudent_Id(
                concluida.getId(), estudante.getId()));
    }

    @Test
    void deveRetornar409AoAvaliarAMesmaSalaDuasVezes() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/class")
                        .with(comoEstudante(estudante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDaSala(concluida.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/evaluations/class")
                        .with(comoEstudante(estudante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDaSala(concluida.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Você já avaliou a sala desta Necessidade."));

        assertEquals(1, classEvaluationRepository.count());
    }

    @Test
    void deveRetornar403QuandoEstudanteNaoAtendeuANecessidade() throws Exception {
        mockMvc.perform(post("/api/v1/evaluations/class")
                        .with(comoEstudante(outroEstudante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDaAvaliacaoDaSala(concluida.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        assertEquals(0, classEvaluationRepository.count());
    }
}
