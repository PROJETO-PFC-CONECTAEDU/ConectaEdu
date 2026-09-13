package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.shared.enums.SchoolStatus;
import com.conectaedu.api.shared.exceptions.InvalidSchoolStatusException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolValidationService {

    private final SchoolRepository schoolRepository;

    //Aprova a escola.
    @Transactional
    public SchoolResponseDTO validateSchool(UUID id, String notes) {
        School school = findPending(id);

        school.setStatus(SchoolStatus.ACTIVE);
        school.setActive(true);
        school.setValidationNotes(notes);
        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);
        return new SchoolResponseDTO(school);
    }

    //Recusa a escola: passa para inactive.
    @Transactional
    public SchoolResponseDTO deactivateSchool(UUID id) {
        School school = findPending(id);

        school.setStatus(SchoolStatus.INACTIVE);
        school.setActive(false);
        school.setValidatedAt(LocalDateTime.now());
        school.setValidationNotes(school.getValidationNotes());
        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);
        return new SchoolResponseDTO(school);

    }

    private School findPending(UUID id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));

        if (school.getStatus() != SchoolStatus.PENDING_ACTIVATION) {
            throw new InvalidSchoolStatusException(
                    "Escola já ativa!");
        }
        return school;
    }
}
