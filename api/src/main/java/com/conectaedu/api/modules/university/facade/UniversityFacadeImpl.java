package com.conectaedu.api.modules.university.facade;

import com.conectaedu.api.modules.university.dto.request.UniversityCreationRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityUpdateRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityValidationRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityCreationResponseDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.interfaces.IUniversityFacade;
import com.conectaedu.api.modules.university.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UniversityFacadeImpl implements IUniversityFacade {

    private final UniversityCreationService universityCreationService;
    private final UniversityUpdateService universityUpdateService;
    private final UniversityDeletionService universityDeletionService;
    private final UniversityListService universityListService;
    private final UniversityListByService universityListByService;
    private final UniversityValidationService universityValidationService;

    public UniversityCreationResponseDTO createUniversity(UniversityCreationRequestDTO request) {
        return universityCreationService.createUniversity(request);
    }

    public UniversityResponseDTO updateUniversity(UUID id, UniversityUpdateRequestDTO request) {
        return universityUpdateService.updateUniversity(id, request);
    }

    public void deleteUniversity(UUID id) {
        universityDeletionService.deleteUniversity(id);
    }

    public UniversityResponseDTO getUniversityById(UUID id) {
        return universityListByService.listById(id);
    }

    public UniversityResponseDTO getUniversityByCnpj(String cnpj) {
        return universityListByService.listByCnpj(cnpj);
    }

    public List<UniversityResponseDTO> getAllUniversities() {
        return universityListService.listAll();
    }

    public UniversityResponseDTO validateUniversity(UUID id, UniversityValidationRequestDTO request) {
        return universityValidationService.validateUniversity(id, request.validationNotes());
    }

    public UniversityResponseDTO rejectUniversity(UUID id, UniversityValidationRequestDTO request) {
        return universityValidationService.rejectUniversity(id, request.validationNotes());
    }
}

