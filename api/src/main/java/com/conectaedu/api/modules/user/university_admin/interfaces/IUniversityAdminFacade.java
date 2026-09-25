package com.conectaedu.api.modules.user.university_admin.interfaces;

import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IUniversityAdminFacade {
    UniversityAdminResponseDTO createUniversityAdmin(UniversityAdminCreationRequestDTO request);
    UniversityAdminResponseDTO updateUniversityAdmin(UUID id, UniversityAdminUpdateRequestDTO request);
    void deleteUniversityAdmin(UUID id);
    UniversityAdminResponseDTO getUniversityAdmin(UUID id);
    List<UniversityAdminResponseDTO> getAllUniversityAdmins();
}
