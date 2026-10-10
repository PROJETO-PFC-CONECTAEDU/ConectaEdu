package com.conectaedu.api.modules.user.platform_admin.service;

import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;
import com.conectaedu.api.modules.user.platform_admin.repository.PlatformAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
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
public class PlatformAdminCreationService {

    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlatformAdminResponseDTO createPlatformAdmin(PlatformAdminCreationRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado!");
        }

        PlatformAdmin admin = PlatformAdmin.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .userType(UserType.PERSON)
                .userRole(UserRole.PLATFORM_ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        platformAdminRepository.save(admin);

        auditService.logCreate(AuditEntityType.USER, admin.getId(), admin.getName());

        return new PlatformAdminResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
