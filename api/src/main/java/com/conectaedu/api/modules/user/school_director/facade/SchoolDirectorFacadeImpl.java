package com.conectaedu.api.modules.user.school_director.facade;

import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorCreationRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorUpdateRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;
import com.conectaedu.api.modules.user.school_director.interfaces.ISchoolDirectorFacade;
import com.conectaedu.api.modules.user.school_director.service.SchoolDirectorCreationService;
import com.conectaedu.api.modules.user.school_director.service.SchoolDirectorDeletionService;
import com.conectaedu.api.modules.user.school_director.service.SchoolDirectorGetService;
import com.conectaedu.api.modules.user.school_director.service.SchoolDirectorUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SchoolDirectorFacadeImpl implements ISchoolDirectorFacade {

    private final SchoolDirectorCreationService creationService;
    private final SchoolDirectorUpdateService updateService;
    private final SchoolDirectorDeletionService deletionService;
    private final SchoolDirectorGetService getService;

    @Override
    public SchoolDirectorResponseDTO createSchoolDirector(SchoolDirectorCreationRequestDTO request) {
        return creationService.createSchoolDirector(request);
    }

    @Override
    public SchoolDirectorResponseDTO updateSchoolDirector(UUID id, SchoolDirectorUpdateRequestDTO request) {
        return updateService.updateSchoolDirector(id, request);
    }

    @Override
    public void deleteSchoolDirector(UUID id) {
        deletionService.deleteSchoolDirector(id);
    }

    @Override
    public SchoolDirectorResponseDTO getSchoolDirector(UUID id) {
        return getService.getSchoolDirector(id);
    }

    @Override
    public List<SchoolDirectorResponseDTO> getAllSchoolDirectors() {
        return getService.getAllSchoolDirectors();
    }
}
