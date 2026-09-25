package com.conectaedu.api.modules.user.platform_admin.interfaces;

import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IPlatformAdminFacade {
    PlatformAdminResponseDTO createPlatformAdmin(PlatformAdminCreationRequestDTO request);
    PlatformAdminResponseDTO updatePlatformAdmin(UUID id, PlatformAdminUpdateRequestDTO request);
    void deletePlatformAdmin(UUID id);
    PlatformAdminResponseDTO getPlatformAdmin(UUID id);
    List<PlatformAdminResponseDTO> getAllPlatformAdmins();
}
