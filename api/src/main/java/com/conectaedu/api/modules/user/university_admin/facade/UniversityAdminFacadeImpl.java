package com.conectaedu.api.modules.user.university_admin.facade;

import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;
import com.conectaedu.api.modules.user.university_admin.interfaces.IUniversityAdminFacade;
import com.conectaedu.api.modules.user.university_admin.service.UniversityAdminCreationService;
import com.conectaedu.api.modules.user.university_admin.service.UniversityAdminDeletionService;
import com.conectaedu.api.modules.user.university_admin.service.UniversityAdminGetService;
import com.conectaedu.api.modules.user.university_admin.service.UniversityAdminUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UniversityAdminFacadeImpl implements IUniversityAdminFacade {

    private final UniversityAdminCreationService creationService;
    private final UniversityAdminUpdateService updateService;
    private final UniversityAdminDeletionService deletionService;
    private final UniversityAdminGetService getService;

    @Override
    public UniversityAdminResponseDTO createUniversityAdmin(UniversityAdminCreationRequestDTO request) {
        return creationService.createUniversityAdmin(request);
    }

    @Override
    public UniversityAdminResponseDTO updateUniversityAdmin(UUID id, UniversityAdminUpdateRequestDTO request) {
        return updateService.updateUniversityAdmin(id, request);
    }

    @Override
    public void deleteUniversityAdmin(UUID id) {
        deletionService.deleteUniversityAdmin(id);
    }

    @Override
    public UniversityAdminResponseDTO getUniversityAdmin(UUID id) {
        return getService.getUniversityAdmin(id);
    }

    @Override
    public List<UniversityAdminResponseDTO> getAllUniversityAdmins() {
        return getService.getAllUniversityAdmins();
    }
}
