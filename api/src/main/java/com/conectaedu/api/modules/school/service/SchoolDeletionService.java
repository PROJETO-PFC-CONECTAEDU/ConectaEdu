package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolDeletionService {

    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    public void deleteSchool(UUID id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));

        schoolRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.SCHOOL, school.getId(), school.getName());
    }
}
