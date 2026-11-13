package com.conectaedu.api.modules.user.genericUser.service;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.dto.request.ChangePasswordRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.request.ProfileUpdateRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, ProfileUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        String beforeName = user.getName();

        user.setName(request.name());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        AuditDiff diff = AuditDiff.create()
                .field("name", beforeName, user.getName());
        auditService.logUpdate(AuditEntityType.USER, user.getId(), user.getName(), diff);

        return new UserResponseDTO(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Senha atual incorreta!");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.logUpdate(AuditEntityType.USER, user.getId(), user.getName(), AuditDiff.create().field("password", "********", "********"));
    }

    @Transactional
    public void deleteOwnAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.logDelete(AuditEntityType.USER, user.getId(), user.getName());
    }

    public Map<String, Object> exportUserData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getUserRole());
        data.put("type", user.getUserType());
        data.put("createdAt", user.getCreatedAt());
        data.put("active", user.isActive());
        data.put("emailConfirmed", user.isEmailConfirmed());

        return data;
    }
}
