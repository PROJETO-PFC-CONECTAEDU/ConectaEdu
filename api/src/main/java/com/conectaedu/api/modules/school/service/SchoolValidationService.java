package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.SchoolStatus;
import com.conectaedu.api.shared.exceptions.InvalidSchoolStatusException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolValidationService {
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    @Transactional
    public SchoolResponseDTO validateSchool(UUID id, String notes) {
        tenancyValidator.validateSchool(id);
        School school = findById(id);

        if (school.getStatus() == SchoolStatus.ACTIVE) {
            throw new InvalidSchoolStatusException("Escola já ativa!");
        }

        SchoolStatus beforeStatus = school.getStatus();
        boolean beforeActive = school.isActive();

        school.setStatus(SchoolStatus.ACTIVE);
        school.setActive(true);
        school.setValidationNotes(notes);
        school.setValidatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, school.getStatus())
                .field("active", beforeActive, school.isActive());
        auditService.logUpdate(AuditEntityType.SCHOOL, school.getId(), school.getName(), diff);

        return new SchoolResponseDTO(school);
    }

    //Recusa a escola: passa para inactive.
    @Transactional
    public SchoolResponseDTO deactivateSchool(UUID id) {
        tenancyValidator.validateSchool(id);
        School school = findById(id);

        if (school.getStatus() == SchoolStatus.INACTIVE) {
            throw new InvalidSchoolStatusException("Escola já está inativa!");
        }

        SchoolStatus beforeStatus = school.getStatus();
        boolean beforeActive = school.isActive();

        school.setStatus(SchoolStatus.INACTIVE);
        school.setActive(false);
        school.setValidatedAt(LocalDateTime.now());
        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, school.getStatus())
                .field("active", beforeActive, school.isActive());
        auditService.logUpdate(AuditEntityType.SCHOOL, school.getId(), school.getName(), diff);

        return new SchoolResponseDTO(school);
    }

    private School findById(UUID id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));
    }
}
