package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.request.UniversityCreationRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityCreationResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.CnpjAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UniversityCreationService {

    private final UniversityRepository universityRepository;
    private final AuditService auditService;

    @Transactional
    public UniversityCreationResponseDTO createUniversity(UniversityCreationRequestDTO request) {

        if (universityRepository.existsByCnpj(request.cnpj())) {
            throw new CnpjAlreadyExistsException("CNPJ já cadastrado!");
        }

        University university = new University();
        university.setName(request.name());
        university.setCnpj(request.cnpj());
        university.setAddress(request.address());
        university.setCoordinator(request.coordinator());

        universityRepository.save(university);

        auditService.logCreate(AuditEntityType.UNIVERSITY, university.getId(), university.getName());

        return new UniversityCreationResponseDTO(
                university.getId(), "Universidade criada com sucesso!");
    }
}
