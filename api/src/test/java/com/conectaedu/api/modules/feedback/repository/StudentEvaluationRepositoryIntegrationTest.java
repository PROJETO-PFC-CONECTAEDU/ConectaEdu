package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.test.database.replace=none")
@ActiveProfiles("test")
class StudentEvaluationRepositoryIntegrationTest {

    @Autowired
    private StudentEvaluationRepository studentEvaluationRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolDirectorRepository schoolDirectorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DemandRepository demandRepository;

    private School escola(String cie) {
        School escola = new School();
        escola.setName("Escola Estadual ABCD");
        escola.setCie(cie);
        escola.setCreatedAt(LocalDateTime.now());
        return schoolRepository.save(escola);
    }

    private SchoolDirector diretor(School escola) {
        SchoolDirector diretor = new SchoolDirector();
        diretor.setName("Joao P");
        diretor.setEmail("diretor.teste@conectaedu.com");
        diretor.setPassword("senha-de-teste");
        diretor.setUserType(UserType.PERSON);
        diretor.setCreatedAt(LocalDateTime.now());
        diretor.setSchool(escola);
        return schoolDirectorRepository.save(diretor);
    }

    private Student estudante(String nome) {
        Student estudante = new Student();
        estudante.setName(nome);
        estudante.setEmail("estudante.teste@conectaedu.com");
        estudante.setPassword("senha-de-teste");
        estudante.setUserType(UserType.PERSON);
        estudante.setCreatedAt(LocalDateTime.now());
        return studentRepository.save(estudante);
    }

    private Demand necessidade(School escola, SchoolDirector diretor, Student estudante) {
        Demand necessidade = new Demand();
        necessidade.setTitle("Reforço de Matemática");
        necessidade.setStatus(DemandStatus.FINISHED);
        necessidade.setSchool(escola);
        necessidade.setDirector(diretor);
        necessidade.setStudent(estudante);
        necessidade.setCreatedAt(LocalDateTime.now());
        necessidade.setFinishedAt(LocalDateTime.now());
        return demandRepository.save(necessidade);
    }

    private StudentEvaluation avaliacao(Demand necessidade, Student estudante, UUID avaliador,
                                        int conhecimento, int pontualidade, int didatica,
                                        int comprometimento, int empatia) {
        StudentEvaluation avaliacao = new StudentEvaluation();
        avaliacao.setDemand(necessidade);
        avaliacao.setStudent(estudante);
        avaliacao.setEvaluatedBy(avaliador);
        avaliacao.setKnowledge(conhecimento);
        avaliacao.setPunctuality(pontualidade);
        avaliacao.setTeaching(didatica);
        avaliacao.setCommitment(comprometimento);
        avaliacao.setEmpathy(empatia);
        avaliacao.setCreatedAt(LocalDateTime.now());
        return avaliacao;
    }

    @Test
    void deveIdentificarAvaliacaoRepetidaApenasParaOMesmoDiretorEstudanteENecessidade() {
        School escola = escola("111111");
        SchoolDirector diretor = diretor(escola);
        Student estudante = estudante("Estudante Teste");
        Student outroEstudante = estudante("Maria Clara");
        Demand necessidade = necessidade(escola, diretor, estudante);
        Demand outraNecessidade = necessidade(escola, diretor, estudante);
        studentEvaluationRepository.save(avaliacao(necessidade, estudante, diretor.getId(), 10, 8, 9, 7, 6));

        boolean mesmaAvaliacao = studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidade.getId(), estudante.getId(), diretor.getId());
        boolean outroDiretor = studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidade.getId(), estudante.getId(), UUID.randomUUID());
        boolean outroAluno = studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidade.getId(), outroEstudante.getId(), diretor.getId());
        boolean outraDemanda = studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                outraNecessidade.getId(), estudante.getId(), diretor.getId());

        assertTrue(mesmaAvaliacao);
        assertFalse(outroDiretor);
        assertFalse(outroAluno);
        assertFalse(outraDemanda);
    }

    //apenas uma avaliacao do diretor para a necessidade deixar de ser pendente no lembrete.
    @Test
    void deveIndicarSeANecessidadeJaRecebeuAvaliacao() {
        School escola = escola("111111");
        SchoolDirector diretor = diretor(escola);
        Student estudante = estudante("Estudante Teste");
        Demand avaliada = necessidade(escola, diretor, estudante);
        Demand pendente = necessidade(escola, diretor, estudante);
        studentEvaluationRepository.save(avaliacao(avaliada, estudante, diretor.getId(), 10, 8, 9, 7, 6));

        boolean jaAvaliada = studentEvaluationRepository.existsByDemand_Id(avaliada.getId());
        boolean aindaPendente = studentEvaluationRepository.existsByDemand_Id(pendente.getId());

        assertTrue(jaAvaliada);
        assertFalse(aindaPendente);
    }

    //a restricao unica do banco segura a duplicidade mesmo que a checagem do service seja burlada.
    @Test
    void deveRecusarNoBancoSegundaAvaliacaoDoMesmoDiretorParaOMesmoEstudante() {
        School escola = escola("111111");
        SchoolDirector diretor = diretor(escola);
        Student estudante = estudante("Estudante Teste");
        Demand necessidade = necessidade(escola, diretor, estudante);
        studentEvaluationRepository.saveAndFlush(
                avaliacao(necessidade, estudante, diretor.getId(), 10, 8, 9, 7, 6));

        assertThrows(DataIntegrityViolationException.class,
                () -> studentEvaluationRepository.saveAndFlush(
                        avaliacao(necessidade, estudante, diretor.getId(), 5, 5, 5, 5, 5)));
    }

    @Test
    void deveCalcularMediaDasAvaliacoesApenasDaEscolaInformada() {
        School escola = escola("111111");
        School outraEscola = escola("222222");
        School escolaSemAvaliacao = escola("333333");
        SchoolDirector diretor = diretor(escola);
        SchoolDirector outroDiretor = diretor(outraEscola);
        Student estudante = estudante("Estudante Teste");
        Demand primeira = necessidade(escola, diretor, estudante);
        Demand segunda = necessidade(escola, diretor, estudante);
        Demand deOutraEscola = necessidade(outraEscola, outroDiretor, estudante);

        //medias 8.0 e 5.0 na escola, 10.0 na outra.
        studentEvaluationRepository.save(avaliacao(primeira, estudante, diretor.getId(), 10, 8, 9, 7, 6));
        studentEvaluationRepository.save(avaliacao(segunda, estudante, diretor.getId(), 5, 5, 5, 5, 5));
        studentEvaluationRepository.save(
                avaliacao(deOutraEscola, estudante, outroDiretor.getId(), 10, 10, 10, 10, 10));

        Double media = studentEvaluationRepository.getAverageScoreBySchool(escola.getId());
        Double mediaDaOutra = studentEvaluationRepository.getAverageScoreBySchool(outraEscola.getId());
        Double semAvaliacao = studentEvaluationRepository.getAverageScoreBySchool(escolaSemAvaliacao.getId());

        assertEquals(6.5, media, 0.001);
        assertEquals(10.0, mediaDaOutra, 0.001);
        assertNull(semAvaliacao);
    }
}
