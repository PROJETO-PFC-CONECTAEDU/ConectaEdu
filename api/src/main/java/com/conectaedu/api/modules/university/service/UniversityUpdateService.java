package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.request.UniversityUpdateRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityUpdateService {

    private final UniversityRepository universityRepository;
    private final AuditService auditService;

    @Transactional
    public UniversityResponseDTO updateUniversity(UUID id, UniversityUpdateRequestDTO request) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        String beforeName = university.getName();
        String beforeCoordinator = university.getCoordinator();
        String beforeAddress = university.getAddress();
        Integer beforeRequiredHours = university.getRequiredInternshipHours();

        university.setName(request.name());
        university.setCoordinator(request.coordinator());
        university.setAddress(request.address());

        //Os três acima são obrigatórios e chegam sempre, a carga é opcional.

        if (request.requiredInternshipHours() != null) {
            university.setRequiredInternshipHours(request.requiredInternshipHours());
        }

        universityRepository.save(university);

        AuditDiff diff = AuditDiff.create()
                .field("name", beforeName, university.getName())
                .field("coordinator", beforeCoordinator, university.getCoordinator())
                .field("address", beforeAddress, university.getAddress())
                .field("requiredInternshipHours", beforeRequiredHours,
                        university.getRequiredInternshipHours());
        auditService.logUpdate(AuditEntityType.UNIVERSITY, university.getId(), university.getName(), diff);

        return new UniversityResponseDTO(university);
    }
}