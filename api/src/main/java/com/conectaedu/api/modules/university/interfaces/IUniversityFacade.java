package com.conectaedu.api.modules.university.interfaces;

import com.conectaedu.api.modules.university.dto.request.UniversityCreationRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityUpdateRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityValidationRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityCreationResponseDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IUniversityFacade {

    UniversityCreationResponseDTO createUniversity(UniversityCreationRequestDTO request);

    UniversityResponseDTO updateUniversity(UUID id, UniversityUpdateRequestDTO request);

    void deleteUniversity(UUID id);

    UniversityResponseDTO getUniversityById(UUID id);

    UniversityResponseDTO getUniversityByCnpj(String cnpj);

    List<UniversityResponseDTO> getAllUniversities();

    UniversityResponseDTO validateUniversity(UUID id, UniversityValidationRequestDTO request);

    UniversityResponseDTO rejectUniversity(UUID id, UniversityValidationRequestDTO request);
}