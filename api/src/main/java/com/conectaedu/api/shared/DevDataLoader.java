package com.conectaedu.api.shared;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void run(String... args) throws Exception{
        createStudentUser("João Salustiano", "admin@conectaedu.com", "123456");
    }


    private void createStudentUser(String name, String email, String password) {
        if (!userRepository.existsByEmail(email)) {
            User user = new Student();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserRole(UserRole.PLATFORM_ADMIN);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("[DEV] Usuário criado: " + email);
        }
    }

}
