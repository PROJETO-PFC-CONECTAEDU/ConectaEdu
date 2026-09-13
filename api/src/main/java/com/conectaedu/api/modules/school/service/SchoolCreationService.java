package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolCreationRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolCreationResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.exceptions.CIEAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SchoolCreationService {


    private final SchoolRepository schoolRepository;

    public SchoolCreationResponseDTO createSchool(SchoolCreationRequestDTO request) {
        if (schoolRepository.existsByCie(request.cie())) {
            throw new CIEAlreadyExistsException("CIE já cadastrado");
        }
        School school = new School();

        school.setName(request.name());

        school.setDirector(request.director());

        school.setCie(request.cie());

        school.setAddress(request.address());

        school.setCreatedAt(LocalDateTime.now());

        schoolRepository.save(school);
        return new SchoolCreationResponseDTO("Escola cadastrada com sucesso!");
    }

}
