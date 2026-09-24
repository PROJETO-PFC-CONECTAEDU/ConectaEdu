package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.dto.request.UserCreationRequestDTO;
import com.conectaedu.api.modules.user.dto.response.UserCreationResponseDTO;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.conectaedu.api.modules.user.domain.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserCreationService {


    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

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

        userRepository.save(user);

        auditService.logCreate(AuditEntityType.USER, user.getId(), user.getName());

        return new UserCreationResponseDTO("Usuário criado com sucesso!");
    }
}
