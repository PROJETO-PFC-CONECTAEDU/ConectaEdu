package com.conectaedu.api.modules.user.genericUser.service;

import com.conectaedu.api.modules.user.genericUser.facade.dto.request.UserCreationRequestDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserCreationResponseDTO;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.email.service.SendEmailConfirmationService;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.conectaedu.api.modules.user.genericUser.domain.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCreationService {


    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final SendEmailConfirmationService emailConfirmationService;

    public UserCreationResponseDTO createUser(UserCreationRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email já cadastrado!");
        }
        User user = new User();

        user.setName(request.name());

        //Email padronizado para lower case
        user.setEmail(request.email().toLowerCase());

        // Criptografia implantada :D
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setCreatedAt(LocalDateTime.now());
        
        user.generateEmailConfirmationToken();

        userRepository.save(user);

        auditService.logCreate(AuditEntityType.USER, user.getId(), user.getName());

        emailConfirmationService.sendConfirmationEmail(user.getEmail(), user.getEmailConfirmationToken());

        return new UserCreationResponseDTO("Usuário criado com sucesso! Verifique seu e-mail para confirmar o cadastro.");
    }
}
