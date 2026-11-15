package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolListByService {
    private final SchoolRepository schoolRepository;
    private final TenancyValidator tenancyValidator;

    public SchoolResponseDTO listById(UUID id) {
        tenancyValidator.validateSchool(id);
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));
        return new SchoolResponseDTO(school);
    }

    public SchoolResponseDTO listByCie(String cie) {
        School school = schoolRepository.findByCie(cie)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));
        tenancyValidator.validateSchool(school.getId());
        return new SchoolResponseDTO(school);
    }
}
