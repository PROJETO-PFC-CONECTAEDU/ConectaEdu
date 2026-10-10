package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolUpdateRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.CIEAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolUpdateService {

    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    public SchoolResponseDTO updateSchool(UUID id, SchoolUpdateRequestDTO request) {
        School school =  schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));

        String beforeName = school.getName();
        String beforeDirector = school.getDirector();
        String beforeAddress = school.getAddress();

        if (request.name() != null) {
            school.setName(request.name());
        }

        if (request.director() != null) {
            school.setDirector(request.director());
        }

        if (request.address() != null) {
            school.setAddress(request.address());
        }

        if (request.latitude() != null) {
            school.setLatitude(request.latitude());
        }

        if (request.longitude() != null) {
            school.setLongitude(request.longitude());
        }

        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);

        AuditDiff diff = AuditDiff.create()
                .field("name", beforeName, school.getName())
                .field("director", beforeDirector, school.getDirector())
                .field("address", beforeAddress, school.getAddress());
        auditService.logUpdate(AuditEntityType.SCHOOL, school.getId(), school.getName(), diff);

        return new SchoolResponseDTO(school);
    }
}
