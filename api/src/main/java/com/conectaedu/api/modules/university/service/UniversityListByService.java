package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityListByService {
    private final UniversityRepository universityRepository;
    private final TenancyValidator tenancyValidator;

    //Somente leitura
    @Transactional(readOnly = true)
    public UniversityResponseDTO listById(UUID id) {
        tenancyValidator.validateUniversity(id);
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));
        return new UniversityResponseDTO(university);
    }

    //Somente leitura
    @Transactional(readOnly = true)
    public UniversityResponseDTO listByCnpj(String cnpj) {
        University university = universityRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));
        tenancyValidator.validateUniversity(university.getId());
        return new UniversityResponseDTO(university);
    }
}
