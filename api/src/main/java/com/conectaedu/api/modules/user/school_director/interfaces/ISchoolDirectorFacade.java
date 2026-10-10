package com.conectaedu.api.modules.user.school_director.interfaces;

import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorCreationRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorUpdateRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ISchoolDirectorFacade {
    SchoolDirectorResponseDTO createSchoolDirector(SchoolDirectorCreationRequestDTO request);
    SchoolDirectorResponseDTO updateSchoolDirector(UUID id, SchoolDirectorUpdateRequestDTO request);
    void deleteSchoolDirector(UUID id);
    SchoolDirectorResponseDTO getSchoolDirector(UUID id);
    List<SchoolDirectorResponseDTO> getAllSchoolDirectors();
}
