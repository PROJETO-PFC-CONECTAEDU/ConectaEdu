package com.conectaedu.api.modules.school.facade;


import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.dto.request.SchoolUpdateRequestDTO;
import com.conectaedu.api.modules.school.dto.request.SchoolValidationRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolCreationResponseDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.interfaces.ISchoolFacade;
import com.conectaedu.api.modules.school.service.*;
import com.conectaedu.api.modules.school.dto.request.SchoolCreationRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SchoolFacadeImpl implements ISchoolFacade {

    private final SchoolCreationService schoolCreationService;
    private final SchoolUpdateService schoolUpdateService;
    private final SchoolDeletionService schoolDeletionService;
    private final SchoolListService schoolListService;
    private final SchoolListByService schoolListByService;
    private final SchoolValidationService schoolValidationService;

    @Override
    public SchoolCreationResponseDTO createSchool(SchoolCreationRequestDTO request) {
        return schoolCreationService.createSchool(request);
    }

    @Override
    public SchoolResponseDTO updateSchool(UUID id, SchoolUpdateRequestDTO request) {
        return schoolUpdateService.updateSchool(id, request);
    }

    @Override
    public void deleteSchool(UUID id) {schoolDeletionService.deleteSchool(id); }

    @Override
    public SchoolResponseDTO getSchoolById(UUID id) {return schoolListByService.listById(id); }

    @Override
    public SchoolResponseDTO getSchoolByCie(String cie) {return schoolListByService.listByCie(cie); }

    @Override
    public List<SchoolResponseDTO> getAllSchools() { return schoolListService.ListAll(); }

    public SchoolResponseDTO activateSchool(UUID id, SchoolValidationRequestDTO request) {
        return schoolValidationService.validateSchool(id, request.validationNotes());
    }

    public SchoolResponseDTO deactivateSchool(UUID id, SchoolValidationRequestDTO request) {
        return schoolValidationService.deactivateSchool(id);
    }

}
