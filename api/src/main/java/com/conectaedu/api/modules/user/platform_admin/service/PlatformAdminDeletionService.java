package com.conectaedu.api.modules.user.platform_admin.service;

import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.modules.user.platform_admin.repository.PlatformAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformAdminDeletionService {

    private final PlatformAdminRepository platformAdminRepository;
    private final AuditService auditService;

    @Transactional
    public void deletePlatformAdmin(UUID id) {
        PlatformAdmin admin = platformAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador da plataforma não encontrado!"));

        platformAdminRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.USER, admin.getId(), admin.getName());
    }
}
