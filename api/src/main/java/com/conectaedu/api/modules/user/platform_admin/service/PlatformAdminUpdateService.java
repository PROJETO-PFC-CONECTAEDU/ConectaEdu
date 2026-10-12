package com.conectaedu.api.modules.user.platform_admin.service;

import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;
import com.conectaedu.api.modules.user.platform_admin.repository.PlatformAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformAdminUpdateService {

    private final UserRepository userRepository;
    private final PlatformAdminRepository platformAdminRepository;
    private final AuditService auditService;

    @Transactional
    public PlatformAdminResponseDTO updatePlatformAdmin(UUID id, PlatformAdminUpdateRequestDTO request) {
        PlatformAdmin admin = platformAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador da plataforma não encontrado!"));

        String beforeEmail = admin.getEmail();
        String beforeName = admin.getName();

        if (request.email() != null && !request.email().trim().equalsIgnoreCase(admin.getEmail())) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException("Email já cadastrado!");
            }
            admin.setEmail(email);
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            admin.setName(request.name().trim());
        }

        admin.setUpdatedAt(LocalDateTime.now());
        platformAdminRepository.save(admin);

        AuditDiff diff = AuditDiff.create()
                .field("email", beforeEmail, admin.getEmail())
                .field("name", beforeName, admin.getName());
        auditService.logUpdate(AuditEntityType.USER, admin.getId(), admin.getName(), diff);

        return new PlatformAdminResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
