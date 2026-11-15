package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityDeletionService {
    private final UniversityRepository universityRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    @Transactional
    public void deleteUniversity(UUID id) {
        tenancyValidator.validateUniversity(id);
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        universityRepository.delete(university);

        auditService.logDelete(AuditEntityType.UNIVERSITY, university.getId(), university.getName());
    }
}
