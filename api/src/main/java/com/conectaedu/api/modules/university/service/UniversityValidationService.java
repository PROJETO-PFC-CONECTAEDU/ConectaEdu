package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.conectaedu.api.shared.exceptions.InvalidUniversityStatusException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityValidationService {
    private final UniversityRepository universityRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    //Aprova a universidade: passa a APPROVED e é ativada.
    @Transactional
    public UniversityResponseDTO validateUniversity(UUID id, String notes) {
        tenancyValidator.validateUniversity(id);
        University university = findById(id);

        if (university.getStatus() == UniversityStatus.APPROVED) {
            throw new InvalidUniversityStatusException("Universidade já aprovada/ativa!");
        }

        UniversityStatus beforeStatus = university.getStatus();
        boolean beforeActive = university.isActive();

        university.setStatus(UniversityStatus.APPROVED);
        university.setActive(true);
        university.setValidatedAt(LocalDateTime.now());
        university.setValidationNotes(notes);
        university.setUpdatedAt(LocalDateTime.now());

        universityRepository.save(university);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, university.getStatus())
                .field("active", beforeActive, university.isActive());
        auditService.logUpdate(AuditEntityType.UNIVERSITY, university.getId(), university.getName(), diff);

        return new UniversityResponseDTO(university);
    }

    //Recusa a universidade: passa a REJECTED e continua inativa.
    @Transactional
    public UniversityResponseDTO rejectUniversity(UUID id, String notes) {
        tenancyValidator.validateUniversity(id);
        //A universidade precisa saber o motivo da recusa.
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException("Recusa exige um parecer justificado");
        }

        University university = findById(id);

        if (university.getStatus() == UniversityStatus.REJECTED) {
            throw new InvalidUniversityStatusException("Universidade já está recusada!");
        }

        UniversityStatus beforeStatus = university.getStatus();
        boolean beforeActive = university.isActive();

        university.setStatus(UniversityStatus.REJECTED);
        university.setActive(false);
        university.setValidatedAt(LocalDateTime.now());
        university.setValidationNotes(notes);
        university.setUpdatedAt(LocalDateTime.now());

        universityRepository.save(university);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, university.getStatus())
                .field("active", beforeActive, university.isActive());
        auditService.logUpdate(AuditEntityType.UNIVERSITY, university.getId(), university.getName(), diff);

        return new UniversityResponseDTO(university);
    }

    @Transactional
    public UniversityResponseDTO deactivateUniversity(UUID id) {
        tenancyValidator.validateUniversity(id);
        University university = findById(id);

        if (university.getStatus() == UniversityStatus.INACTIVE) {
            throw new InvalidUniversityStatusException("Universidade já está inativa!");
        }

        UniversityStatus beforeStatus = university.getStatus();
        boolean beforeActive = university.isActive();

        university.setStatus(UniversityStatus.INACTIVE);
        university.setActive(false);
        university.setValidatedAt(LocalDateTime.now());
        university.setUpdatedAt(LocalDateTime.now());

        universityRepository.save(university);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, university.getStatus())
                .field("active", beforeActive, university.isActive());
        auditService.logUpdate(AuditEntityType.UNIVERSITY, university.getId(), university.getName(), diff);

        return new UniversityResponseDTO(university);
    }

    private University findById(UUID id) {
        return universityRepository.findById(id)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));
    }
}
