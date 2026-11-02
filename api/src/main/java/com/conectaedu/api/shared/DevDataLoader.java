package com.conectaedu.api.shared;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.lesson.repository.LessonRepository;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final SchoolRepository schoolRepository;
    private final DemandRepository demandRepository;
    private final LessonRepository lessonRepository;
    private final StudentEvaluationRepository studentEvaluationRepository;
    private final ClassEvaluationRepository classEvaluationRepository;
    private final PasswordEncoder passwordEncoder;


    public void run(String... args) throws Exception{
        createAdminUser("João Salustiano", "admin@conectaedu.com", "123456");

        University university = createDefaultUniversity();
        School school = createDefaultSchool();

        createUniversityAdmin("Admin Uni", "uni_admin@conectaedu.com", "123456", university);
        SchoolDirector director = createSchoolDirector("Diretor Escola", "director@conectaedu.com", "123456", school);
        Student student = createStudent("Estudante Teste", "student@conectaedu.com", "123456", university);

        createDefaultDemands(school, director);
        createInternshipFlowData(school, director, student);
    }


    private void createAdminUser(String name, String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            User user = new PlatformAdmin();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserRole(UserRole.PLATFORM_ADMIN);
            user.setUserType(UserType.PERSON);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("[DEV] Usuário Platform Admin criado: " + email);
        }
    }

    private University createDefaultUniversity() {
        String cnpj = "12345678000199";
        return universityRepository.findByCnpj(cnpj).orElseGet(() -> {
            University university = new University();
            university.setName("Universidade Federal de Tecnologia");
            university.setCnpj(cnpj);
            university.setAddress("Av. das Universidades, 1000");
            university.setCoordinator("Dr. Carlos Silva");
            university.setStatus(UniversityStatus.APPROVED);
            university.setActive(true);
            university.setRequiredInternshipHours(120);
            university.setCreatedAt(LocalDateTime.now());
            university.setUpdatedAt(LocalDateTime.now());
            System.out.println("[DEV] Universidade criada: " + university.getName());
            return universityRepository.save(university);
        });
    }

    private School createDefaultSchool() {
        String cie = "987654";
        return schoolRepository.findByCie(cie).orElseGet(() -> {
            School school = new School();
            school.setName("Escola Estadual Padrão");
            school.setCie(cie);
            school.setAddress("Rua das Escolas, 500");
            school.setLatitude(-23.55052);
            school.setLongitude(-46.633308);
            school.setDirector("Maria Oliveira");
            school.setStatus(SchoolStatus.ACTIVE);
            school.setActive(true);
            school.setCreatedAt(LocalDateTime.now());
            school.setUpdatedAt(LocalDateTime.now());
            System.out.println("[DEV] Escola criada: " + school.getName());
            return schoolRepository.save(school);
        });
    }

    private void createUniversityAdmin(String name, String email, String password, University university) {
        if (!userRepository.existsByEmail(email)) {
            UniversityAdmin user = new UniversityAdmin();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserRole(UserRole.UNIVERSITY_ADMIN);
            user.setUserType(UserType.PERSON);
            user.setUniversity(university);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("[DEV] Usuário University Admin criado: " + email);
        }
    }

    private SchoolDirector createSchoolDirector(String name, String email, String password, School school) {
        return (SchoolDirector) userRepository.findByEmail(email).orElseGet(() -> {
            SchoolDirector user = new SchoolDirector();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserRole(UserRole.SCHOOL_DIRECTOR);
            user.setUserType(UserType.PERSON);
            user.setSchool(school);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("[DEV] Usuário School Director criado: " + email);
            return user;
        });
    }

    private void createDefaultDemands(School school, SchoolDirector director) {
        if (demandRepository.count() == 0) {
            Demand demand1 = new Demand();
            demand1.setTitle("Reforço em Matemática - 6º Ano");
            demand1.setDescription("Necessidade de monitoria para alunos com dificuldade em frações e decimais.");
            demand1.setSubject("Matemática");
            demand1.setGradeLevel("6º Ano");
            demand1.setPupilAmount(15);
            demand1.setRoom("Sala 12");
            demand1.setDifficultyLevel("Médio");
            demand1.setStatus(DemandStatus.WAITING);
            demand1.setClassDate(LocalDateTime.now().plusDays(7));
            demand1.setTotalHours("2h");
            demand1.setSchool(school);
            demand1.setDirector(director);
            demand1.setCreatedAt(LocalDateTime.now());
            demand1.setUpdatedAt(LocalDateTime.now());

            Demand demand2 = new Demand();
            demand2.setTitle("Oficina de Leitura - 8º Ano");
            demand2.setDescription("Apoio pedagógico para incentivo à leitura e interpretação de texto.");
            demand2.setSubject("Português");
            demand2.setGradeLevel("8º Ano");
            demand2.setPupilAmount(10);
            demand2.setRoom("Biblioteca");
            demand2.setDifficultyLevel("Fácil");
            demand2.setStatus(DemandStatus.WAITING);
            demand2.setClassDate(LocalDateTime.now().plusDays(10));
            demand2.setTotalHours("1h30");
            demand2.setSchool(school);
            demand2.setDirector(director);
            demand2.setCreatedAt(LocalDateTime.now());
            demand2.setUpdatedAt(LocalDateTime.now());

            demandRepository.saveAll(List.of(demand1, demand2));
            System.out.println("[DEV] Demandas de teste criadas.");
        }
    }

    private Student createStudent(String name, String email, String password, University university) {
        return (Student) userRepository.findByEmail(email).orElseGet(() -> {
            Student user = new Student();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserRole(UserRole.STUDENT);
            user.setUserType(UserType.PERSON);
            user.setUniversity(university);
            user.setStatus(StudentStatus.VALIDATED);
            user.setAvailability("Manhã e Tarde");
            user.setInterestAreas(List.of("Matemática", "Física"));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("[DEV] Usuário Student criado: " + email);
            return user;
        });
    }

    private void createInternshipFlowData(School school, SchoolDirector director, Student student) {
        if (lessonRepository.count() > 0) return;

        // 1. Demanda ONGOING (Em andamento) para o estudante avaliar a turma
        Demand ongoingDemand = new Demand();
        ongoingDemand.setTitle("Reforço de Química - 2º Ano Médio");
        ongoingDemand.setDescription("Monitoria de química orgânica.");
        ongoingDemand.setSubject("Química");
        ongoingDemand.setGradeLevel("2º Ano Médio");
        ongoingDemand.setPupilAmount(12);
        ongoingDemand.setRoom("Laboratório 2");
        ongoingDemand.setDifficultyLevel("Médio");
        ongoingDemand.setStatus(DemandStatus.ONGOING);
        ongoingDemand.setClassDate(LocalDateTime.now().minusDays(2));
        ongoingDemand.setTotalHours("20h");
        ongoingDemand.setSchool(school);
        ongoingDemand.setDirector(director);
        ongoingDemand.setStudent(student);
        ongoingDemand.setCreatedAt(LocalDateTime.now().minusDays(10));
        ongoingDemand.setUpdatedAt(LocalDateTime.now());
        demandRepository.save(ongoingDemand);

        // 2. Aulas para compor o progresso de horas (Estágio)
        // Aula Validada (Soma horas)
        Lesson lesson1 = new Lesson();
        lesson1.setSubject("Química");
        lesson1.setScheduledStart(LocalDateTime.now().minusDays(5).withHour(14).withMinute(0));
        lesson1.setScheduledEnd(LocalDateTime.now().minusDays(5).withHour(16).withMinute(0));
        lesson1.setStatus(LessonStatus.VALIDATED);
        lesson1.setDemand(ongoingDemand);
        lesson1.setStudent(student);
        lesson1.setSchool(school);
        lesson1.setSignedAt(LocalDateTime.now().minusDays(4));
        lesson1.setSignedBy(director.getId());
        lesson1.setCreatedAt(LocalDateTime.now().minusDays(5));
        lessonRepository.save(lesson1);

        // Aula Pendente de Assinatura (Status COMPLETED)
        Lesson lesson2 = new Lesson();
        lesson2.setSubject("Química");
        lesson2.setScheduledStart(LocalDateTime.now().minusDays(3).withHour(14).withMinute(0));
        lesson2.setScheduledEnd(LocalDateTime.now().minusDays(3).withHour(16).withMinute(0));
        lesson2.setStatus(LessonStatus.COMPLETED);
        lesson2.setDemand(ongoingDemand);
        lesson2.setStudent(student);
        lesson2.setSchool(school);
        lesson2.setCreatedAt(LocalDateTime.now().minusDays(3));
        lessonRepository.save(lesson2);

        // 3. Demanda FINISHED (Concluída) com avaliações
        Demand finishedDemand = new Demand();
        finishedDemand.setTitle("Projeto Robótica - 9º Ano");
        finishedDemand.setDescription("Introdução a Arduino.");
        finishedDemand.setSubject("Ciências");
        finishedDemand.setGradeLevel("9º Ano");
        finishedDemand.setPupilAmount(8);
        finishedDemand.setRoom("Maker Space");
        finishedDemand.setDifficultyLevel("Difícil");
        finishedDemand.setStatus(DemandStatus.FINISHED);
        finishedDemand.setFinishedAt(LocalDateTime.now().minusDays(15));
        finishedDemand.setTotalHours("10h");
        finishedDemand.setSchool(school);
        finishedDemand.setDirector(director);
        finishedDemand.setStudent(student);
        finishedDemand.setCreatedAt(LocalDateTime.now().minusDays(30));
        finishedDemand.setUpdatedAt(LocalDateTime.now().minusDays(15));
        demandRepository.save(finishedDemand);

        // Avaliação do Diretor sobre o Estudante (Feedback Recebido)
        StudentEvaluation studentEval = new StudentEvaluation();
        studentEval.setDemand(finishedDemand);
        studentEval.setStudent(student);
        studentEval.setEvaluatedBy(director.getId());
        studentEval.setKnowledge(9);
        studentEval.setPunctuality(10);
        studentEval.setTeaching(8);
        studentEval.setCommitment(10);
        studentEval.setEmpathy(9);
        studentEval.setComments("O estudante demonstrou excelente domínio técnico e ótima relação com os alunos.");
        studentEval.setCreatedAt(LocalDateTime.now().minusDays(14));
        studentEvaluationRepository.save(studentEval);

        // Avaliação do Estudante sobre a Turma (Feedback Enviado)
        ClassEvaluation classEval = new ClassEvaluation();
        classEval.setDemand(finishedDemand);
        classEval.setStudent(student);
        classEval.setEngagement(8);
        classEval.setProgress(7);
        classEval.setInfrastructure(9);
        classEval.setComments("A escola ofereceu ótimos recursos, embora os alunos tenham tido alguma dificuldade inicial com a lógica.");
        classEval.setCreatedAt(LocalDateTime.now().minusDays(14));
        classEvaluationRepository.save(classEval);

        System.out.println("[DEV] Dados de fluxo de estágio e feedback criados.");
    }

}
