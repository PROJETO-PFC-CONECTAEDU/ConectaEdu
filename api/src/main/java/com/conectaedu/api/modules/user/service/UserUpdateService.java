package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.modules.user.dto.request.UserUpdateRequestDTO;
import com.conectaedu.api.modules.user.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        String beforeEmail = user.getEmail();
        String beforeName = user.getName();
        var beforeUserType = user.getUserType();

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.email().toLowerCase())) {
                throw new EmailAlreadyExistsException("Email já cadastrado!");
            }
            user.setEmail(request.email().toLowerCase());
        }

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.userType() != null) {
            user.setUserType(request.userType());
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        AuditDiff diff = AuditDiff.create()
                .field("email", beforeEmail, user.getEmail())
                .field("name", beforeName, user.getName())
                .field("userType", beforeUserType, user.getUserType());
        auditService.logUpdate(AuditEntityType.USER, user.getId(), user.getName(), diff);

        return new UserResponseDTO(user);
    }
}
