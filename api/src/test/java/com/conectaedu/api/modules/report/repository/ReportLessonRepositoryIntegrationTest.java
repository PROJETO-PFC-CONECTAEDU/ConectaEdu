package com.conectaedu.api.modules.report.repository;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
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
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.test.database.replace=none",
        "spring.datasource.url=jdbc:h2:mem:report-repository"
})
@ActiveProfiles("test")
class ReportLessonRepositoryIntegrationTest {

    private static final List<LessonStatus> SITUACOES_DO_RELATORIO = List.of(
            LessonStatus.COMPLETED, LessonStatus.VALIDATED,
            LessonStatus.CANCELED, LessonStatus.NOT_COMPLETED);

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 3, 1, 0, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 3, 31, 23, 59, 59);

    @Autowired
    private ReportLessonRepository reportLessonRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolDirectorRepository schoolDirectorRepository;

    @Autowired
    private StudentRepository studentRepository;

    private School escola;
    private Demand necessidade;
    private Student estudante;

    @BeforeEach
    void prepararCenario() {
        escola = new School();
        escola.setName("Escola ABCD");
        escola.setCie("123456");
        escola.setCreatedAt(LocalDateTime.now());
        escola = schoolRepository.save(escola);

        SchoolDirector diretor = schoolDirectorRepository.save(SchoolDirector.builder()
                .name("Joao P")
                .email("diretor.teste@conectaedu.com")
                .password("senha-de-teste")
                .userType(UserType.PERSON)
                .createdAt(LocalDateTime.now())
                .school(escola)
                .build());

        necessidade = new Demand();
        necessidade.setTitle("Reforço de matemática");
        necessidade.setStatus(DemandStatus.ONGOING);
        necessidade.setSchool(escola);
        necessidade.setDirector(diretor);
        necessidade.setCreatedAt(LocalDateTime.now());
        necessidade = demandRepository.save(necessidade);

        estudante = estudante("Maria S", "maria.teste@conectaedu.com");
    }

    private Student estudante(String nome, String email) {
        return studentRepository.save(Student.builder()
                .name(nome)
                .email(email)
                .password("senha-de-teste")
                .userType(UserType.PERSON)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Lesson aula(Student estudante, LessonStatus situacao, LocalDateTime termino) {
        Lesson aula = new Lesson();
        aula.setSubject("Matemática");
        aula.setScheduledStart(termino.minusHours(1));
        aula.setScheduledEnd(termino);
        aula.setStatus(situacao);
        aula.setDemand(necessidade);
        aula.setStudent(estudante);
        aula.setSchool(escola);
        return lessonRepository.save(aula);
    }

    private LocalDateTime dia(int dia) {
        return LocalDateTime.of(2026, 3, dia, 9, 0);
    }

    @Test
    void deveTrazerApenasAulasDoEstudanteNasSituacoesDoRelatorio() {
        Student outroEstudante = estudante("Pedro L", "pedro.teste@conectaedu.com");
        aula(estudante, LessonStatus.VALIDATED, dia(2));
        aula(estudante, LessonStatus.COMPLETED, dia(3));
        aula(estudante, LessonStatus.CANCELED, dia(4));
        aula(estudante, LessonStatus.NOT_COMPLETED, dia(5));
        aula(estudante, LessonStatus.SCHEDULED, dia(6));
        aula(estudante, LessonStatus.CONFIRMED, dia(7));
        aula(outroEstudante, LessonStatus.VALIDATED, dia(8));

        List<Lesson> aulas = reportLessonRepository
                .findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                        estudante.getId(), SITUACOES_DO_RELATORIO, INICIO, FIM);

        assertEquals(4, aulas.size());
        assertTrue(aulas.stream().allMatch(a -> a.getStudent().getId().equals(estudante.getId())));
        assertTrue(aulas.stream().allMatch(a -> SITUACOES_DO_RELATORIO.contains(a.getStatus())));
        assertEquals(7, lessonRepository.count());
    }

    @Test
    void deveRespeitarOPeriodoEOrdenarPeloTerminoDaAula() {
        Lesson ultima = aula(estudante, LessonStatus.VALIDATED, dia(20));
        Lesson primeira = aula(estudante, LessonStatus.VALIDATED, dia(5));
        Lesson doMeio = aula(estudante, LessonStatus.COMPLETED, dia(12));
        aula(estudante, LessonStatus.VALIDATED, INICIO.minusDays(1));
        aula(estudante, LessonStatus.VALIDATED, FIM.plusDays(1));

        List<Lesson> aulas = reportLessonRepository
                .findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                        estudante.getId(), SITUACOES_DO_RELATORIO, INICIO, FIM);

        assertEquals(3, aulas.size());
        assertEquals(primeira.getId(), aulas.get(0).getId());
        assertEquals(doMeio.getId(), aulas.get(1).getId());
        assertEquals(ultima.getId(), aulas.get(2).getId());
    }

    @Test
    void deveDevolverListaVaziaQuandoEstudanteNaoTemAulaNoPeriodo() {
        aula(estudante, LessonStatus.VALIDATED, INICIO.minusMonths(2));

        List<Lesson> aulas = reportLessonRepository
                .findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                        estudante.getId(), SITUACOES_DO_RELATORIO, INICIO, FIM);

        assertTrue(aulas.isEmpty());
    }
}
