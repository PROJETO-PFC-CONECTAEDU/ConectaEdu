package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolUpdateRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
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

    public SchoolResponseDTO updateSchool(UUID id, SchoolUpdateRequestDTO request) {
        School school =  schoolRepository.findById(id)
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));

        if (request.address() != null && !request.address().equalsIgnoreCase(school.getAddress())) {
            if (schoolRepository.existsByCie(school.getCie())) {
                throw new CIEAlreadyExistsException("Endereço já cadastrado!");
            }
            school.setAddress(request.address());
        }

        if (request.name() != null) {
            school.setName( request.name());
        }

        school.setUpdatedAt(LocalDateTime.now());

        schoolRepository.save(school);

        return new SchoolResponseDTO(school);
    }
}
