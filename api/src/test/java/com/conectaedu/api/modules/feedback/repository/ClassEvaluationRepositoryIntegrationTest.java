package com.conectaedu.api.modules.feedback.repository;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.test.database.replace=none")
@ActiveProfiles("test")
class ClassEvaluationRepositoryIntegrationTest {

    @Autowired
    private ClassEvaluationRepository classEvaluationRepository;

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

    private ClassEvaluation avaliacao(Demand necessidade, Student estudante,
                                      int engajamento, int evolucao, int infraestrutura) {
        ClassEvaluation avaliacao = new ClassEvaluation();
        avaliacao.setDemand(necessidade);
        avaliacao.setStudent(estudante);
        avaliacao.setEngagement(engajamento);
        avaliacao.setProgress(evolucao);
        avaliacao.setInfrastructure(infraestrutura);
        avaliacao.setCreatedAt(LocalDateTime.now());
        return avaliacao;
    }

    @Test
    void deveIdentificarAvaliacaoRepetidaApenasParaOMesmoEstudanteENecessidade() {
        School escola = escola("111111");
        SchoolDirector diretor = diretor(escola);
        Student estudante = estudante("Estudante Teste");
        Student outroEstudante = estudante("Maria Clara");
        Demand necessidade = necessidade(escola, diretor, estudante);
        Demand outraNecessidade = necessidade(escola, diretor, estudante);
        classEvaluationRepository.save(avaliacao(necessidade, estudante, 8, 6, 10));

        boolean mesmaAvaliacao = classEvaluationRepository.existsByDemand_IdAndStudent_Id(
                necessidade.getId(), estudante.getId());
        boolean outroAluno = classEvaluationRepository.existsByDemand_IdAndStudent_Id(
                necessidade.getId(), outroEstudante.getId());
        boolean outraDemanda = classEvaluationRepository.existsByDemand_IdAndStudent_Id(
                outraNecessidade.getId(), estudante.getId());

        assertTrue(mesmaAvaliacao);
        assertFalse(outroAluno);
        assertFalse(outraDemanda);
    }

    @Test
    void deveRecusarNoBancoSegundaAvaliacaoDoMesmoEstudanteParaAMesmaSala() {
        School escola = escola("111111");
        SchoolDirector diretor = diretor(escola);
        Student estudante = estudante("Estudante Teste");
        Demand necessidade = necessidade(escola, diretor, estudante);
        classEvaluationRepository.saveAndFlush(avaliacao(necessidade, estudante, 8, 6, 10));

        assertThrows(DataIntegrityViolationException.class,
                () -> classEvaluationRepository.saveAndFlush(avaliacao(necessidade, estudante, 5, 5, 5)));
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
        classEvaluationRepository.save(avaliacao(primeira, estudante, 8, 6, 10));
        classEvaluationRepository.save(avaliacao(segunda, estudante, 4, 5, 6));
        classEvaluationRepository.save(avaliacao(deOutraEscola, estudante, 10, 10, 10));

        Double media = classEvaluationRepository.getAverageScoreBySchool(escola.getId());
        Double mediaDaOutra = classEvaluationRepository.getAverageScoreBySchool(outraEscola.getId());
        Double semAvaliacao = classEvaluationRepository.getAverageScoreBySchool(escolaSemAvaliacao.getId());

        assertEquals(6.5, media, 0.001);
        assertEquals(10.0, mediaDaOutra, 0.001);
        assertNull(semAvaliacao);
    }
}
