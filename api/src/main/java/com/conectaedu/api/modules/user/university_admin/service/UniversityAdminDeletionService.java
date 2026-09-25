package com.conectaedu.api.modules.user.university_admin.service;

import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityAdminDeletionService {

    private final UniversityAdminRepository universityAdminRepository;
    private final AuditService auditService;

    @Transactional
    public void deleteUniversityAdmin(UUID id) {
        UniversityAdmin admin = universityAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador de universidade não encontrado!"));

        universityAdminRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.UNIVERSITY_ADMIN, admin.getId(), admin.getName());
    }
}
