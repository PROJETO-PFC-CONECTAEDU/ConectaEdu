package com.conectaedu.api.shared;

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
    private final PasswordEncoder passwordEncoder;


    public void run(String... args) throws Exception{
        createAdminUser("João Salustiano", "admin@conectaedu.com", "123456");
        
        University university = createDefaultUniversity();
        School school = createDefaultSchool();
        
        createUniversityAdmin("Admin Uni", "uni_admin@conectaedu.com", "123456", university);
        createSchoolDirector("Diretor Escola", "director@conectaedu.com", "123456", school);
        createStudent("Estudante Teste", "student@conectaedu.com", "123456", university);
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

    private void createSchoolDirector(String name, String email, String password, School school) {
        if (!userRepository.existsByEmail(email)) {
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
        }
    }

    private void createStudent(String name, String email, String password, University university) {
        if (!userRepository.existsByEmail(email)) {
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
        }
    }

}
