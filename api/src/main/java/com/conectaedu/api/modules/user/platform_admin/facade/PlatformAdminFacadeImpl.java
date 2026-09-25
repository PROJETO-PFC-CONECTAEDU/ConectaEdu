package com.conectaedu.api.modules.user.platform_admin.facade;

import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;
import com.conectaedu.api.modules.user.platform_admin.interfaces.IPlatformAdminFacade;
import com.conectaedu.api.modules.user.platform_admin.service.PlatformAdminCreationService;
import com.conectaedu.api.modules.user.platform_admin.service.PlatformAdminDeletionService;
import com.conectaedu.api.modules.user.platform_admin.service.PlatformAdminGetService;
import com.conectaedu.api.modules.user.platform_admin.service.PlatformAdminUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatformAdminFacadeImpl implements IPlatformAdminFacade {

    private final PlatformAdminCreationService creationService;
    private final PlatformAdminUpdateService updateService;
    private final PlatformAdminDeletionService deletionService;
    private final PlatformAdminGetService getService;

    @Override
    public PlatformAdminResponseDTO createPlatformAdmin(PlatformAdminCreationRequestDTO request) {
        return creationService.createPlatformAdmin(request);
    }

    @Override
    public PlatformAdminResponseDTO updatePlatformAdmin(UUID id, PlatformAdminUpdateRequestDTO request) {
        return updateService.updatePlatformAdmin(id, request);
    }

    @Override
    public void deletePlatformAdmin(UUID id) {
        deletionService.deletePlatformAdmin(id);
    }

    @Override
    public PlatformAdminResponseDTO getPlatformAdmin(UUID id) {
        return getService.getPlatformAdmin(id);
    }

    @Override
    public List<PlatformAdminResponseDTO> getAllPlatformAdmins() {
        return getService.getAllPlatformAdmins();
    }
}
