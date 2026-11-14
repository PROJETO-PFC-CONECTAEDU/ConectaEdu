package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.UniversityAdminRegisterDTO;
import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.shared.email.service.SendEmailConfirmationService;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.BusinessException;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterUniversityAdminService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendEmailConfirmationService emailConfirmationService;

    @Transactional
    public void register(UniversityAdminRegisterDTO data) {
        if (userRepository.existsByEmail(data.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        UniversityAdmin admin = UniversityAdmin.builder()
                .name(data.name())
                .email(data.email().toLowerCase())
                .password(passwordEncoder.encode(data.password()))
                .userType(UserType.PERSON)
                .userRole(UserRole.UNIVERSITY_ADMIN)
                .university(null)
                .createdAt(LocalDateTime.now())
                .emailConfirmed(false)
                .active(false)
                .build();

        admin.generateEmailConfirmationToken();
        userRepository.save(admin);
        emailConfirmationService.sendConfirmationEmail(admin.getEmail(), admin.getEmailConfirmationToken());
    }
}
