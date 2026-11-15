package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.SchoolDirectorRegisterDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.shared.email.service.SendEmailConfirmationService;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterDirectorService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendEmailConfirmationService emailConfirmationService;

    @Transactional
    public void register(SchoolDirectorRegisterDTO data) {
        if (userRepository.existsByEmail(data.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        SchoolDirector director = SchoolDirector.builder()
                .name(data.name())
                .email(data.email().toLowerCase())
                .password(passwordEncoder.encode(data.password()))
                .userType(UserType.PERSON)
                .school(null)
                .userRole(UserRole.SCHOOL_DIRECTOR)
                .createdAt(LocalDateTime.now())
                .emailConfirmed(false)
                .active(false)
                .build();

        director.generateEmailConfirmationToken();
        userRepository.save(director);
        emailConfirmationService.sendConfirmationEmail(director.getEmail(), director.getEmailConfirmationToken());
    }
}
