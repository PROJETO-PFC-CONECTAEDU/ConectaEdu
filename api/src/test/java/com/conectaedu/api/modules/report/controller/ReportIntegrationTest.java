package com.conectaedu.api.modules.report.controller;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//o cenario envolve usuario, escola, necessidade, aula e avaliacao.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {

    private static final RequestPostProcessor ADMIN =
            SecurityMockMvcRequestPostProcessors.user("admin").roles("PLATFORM_ADMIN");

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
    private LessonRepository lessonRepository;

    @Autowired
    private StudentEvaluationRepository studentEvaluationRepository;

    private School escola;
    private SchoolDirector diretor;
    private Student estudante;
    private Demand necessidade;

    @BeforeEach
    void prepararCenario() {
        escola = new School();
        escola.setName("Escola ABCD");
        escola.setCie("123456");
        escola.setCreatedAt(LocalDateTime.now());
        escola = schoolRepository.save(escola);

        diretor = schoolDirectorRepository.save(SchoolDirector.builder()
                .name("Joao P")
                .email("diretor.teste@conectaedu.com")
                .password("senha-de-teste")
                .userType(UserType.PERSON)
                .createdAt(LocalDateTime.now())
                .school(escola)
                .build());

        estudante = studentRepository.save(Student.builder()
                .name("Maria S")
                .email("maria.teste@conectaedu.com")
                .password("senha-de-teste")
                .userType(UserType.PERSON)
                .createdAt(LocalDateTime.now())
                .build());

        necessidade = new Demand();
        necessidade.setTitle("Reforço de matemática");
        necessidade.setStatus(DemandStatus.FINISHED);
        necessidade.setSchool(escola);
        necessidade.setDirector(diretor);
        necessidade.setStudent(estudante);
        necessidade.setCreatedAt(LocalDateTime.of(2026, 2, 1, 8, 0));
        necessidade.setFinishedAt(LocalDateTime.of(2026, 3, 20, 12, 0));
        necessidade = demandRepository.save(necessidade);

        Lesson validada = aula(LessonStatus.VALIDATED, LocalDateTime.of(2026, 3, 2, 8, 0), 90);
        validada.setNineBoxPerformance(2);
        validada.setNineBoxPotential(3);
        validada.setSignedAt(LocalDateTime.of(2026, 3, 3, 10, 0));
        lessonRepository.save(validada);

        lessonRepository.save(aula(LessonStatus.COMPLETED, LocalDateTime.of(2026, 3, 9, 8, 0), 60));
        lessonRepository.save(aula(LessonStatus.CANCELED, LocalDateTime.of(2026, 3, 16, 8, 0), 60));

        StudentEvaluation avaliacao = new StudentEvaluation();
        avaliacao.setDemand(necessidade);
        avaliacao.setStudent(estudante);
        avaliacao.setEvaluatedBy(diretor.getId());
        avaliacao.setKnowledge(5);
        avaliacao.setPunctuality(4);
        avaliacao.setTeaching(4);
        avaliacao.setCommitment(4);
        avaliacao.setEmpathy(3);
        avaliacao.setComments("Bom trabalho");
        avaliacao.setCreatedAt(LocalDateTime.of(2026, 3, 21, 9, 0));
        studentEvaluationRepository.save(avaliacao);
    }

    private Lesson aula(LessonStatus situacao, LocalDateTime inicio, int minutos) {
        Lesson aula = new Lesson();
        aula.setSubject("Matemática");
        aula.setScheduledStart(inicio);
        aula.setScheduledEnd(inicio.plusMinutes(minutos));
        aula.setStatus(situacao);
        aula.setDemand(necessidade);
        aula.setStudent(estudante);
        aula.setSchool(escola);
        return aula;
    }

    //caminho feliz 200.
    @Test
    void deveRetornar200ComORelatorioDoEstudanteNoPeriodo() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId())
                        .with(ADMIN)
                        .param("from", "2026-03-01")
                        .param("to", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(estudante.getId().toString()))
                .andExpect(jsonPath("$.studentName").value("Maria S"))
                .andExpect(jsonPath("$.totalHours").value(1.5))
                .andExpect(jsonPath("$.requiredHours").value(400))
                .andExpect(jsonPath("$.remainingHours").value(398.5))
                .andExpect(jsonPath("$.performedLessons").value(2))
                .andExpect(jsonPath("$.validatedHoursInPeriod").value(1.5))
                .andExpect(jsonPath("$.pendingHoursInPeriod").value(1.0))
                .andExpect(jsonPath("$.canceledLessons").value(1))
                .andExpect(jsonPath("$.notCompletedLessons").value(0))
                .andExpect(jsonPath("$.averageReceived").value(4.0))
                .andExpect(jsonPath("$.evaluations.length()").value(1))
                .andExpect(jsonPath("$.evaluations[0].demandTitle").value("Reforço de matemática"));
    }

    @Test
    void deveManterCargaDoEstagioQuandoPeriodoNaoTemAtividade() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId())
                        .with(ADMIN)
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHours").value(1.5))
                .andExpect(jsonPath("$.performedLessons").value(0))
                .andExpect(jsonPath("$.validatedHoursInPeriod").value(0.0))
                .andExpect(jsonPath("$.canceledLessons").value(0))
                .andExpect(jsonPath("$.averageReceived").doesNotExist())
                .andExpect(jsonPath("$.evaluations").isEmpty());
    }

    @Test
    void deveRetornar200ComHistoricoInteiroQuandoPeriodoNaoForInformado() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId()).with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.performedLessons").value(2))
                .andExpect(jsonPath("$.canceledLessons").value(1))
                .andExpect(jsonPath("$.averageReceived").value(4.0));
    }

    //cenario de erro 404.
    @Test
    void deveRetornar404ComCorpoDeErroQuandoEstudanteNaoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", UUID.randomUUID()).with(ADMIN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Estudante não encontrado!"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar404QuandoEscolaDoFiltroNaoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId())
                        .with(ADMIN)
                        .param("schoolId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Escola não encontrada!"));
    }

    //erro de validacao.
    @Test
    void deveRetornar400QuandoDataDeInicioForPosteriorAoTermino() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId())
                        .with(ADMIN)
                        .param("from", "2026-03-31")
                        .param("to", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("A data de início não pode ser posterior à data de término."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}", estudante.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar200ComASerieDoNineBox() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}/progress", estudante.getId())
                        .with(ADMIN)
                        .param("schoolId", escola.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lessonId").exists())
                .andExpect(jsonPath("$[0].performance").value(2))
                .andExpect(jsonPath("$[0].potential").value(3));
    }

    @Test
    void deveRetornar404NaSerieQuandoEstudanteNaoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/reports/students/{id}/progress", UUID.randomUUID()).with(ADMIN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Estudante não encontrado!"));
    }
}
