package com.conectaedu.api.modules.user.platform_admin.service;

import com.conectaedu.api.modules.user.platform_admin.domain.PlatformAdmin;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;
import com.conectaedu.api.modules.user.platform_admin.repository.PlatformAdminRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformAdminGetService {

    private final PlatformAdminRepository platformAdminRepository;

    @Transactional(readOnly = true)
    public List<PlatformAdminResponseDTO> getAllPlatformAdmins() {
        return platformAdminRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformAdminResponseDTO getPlatformAdmin(UUID id) {
        PlatformAdmin admin = platformAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador da plataforma não encontrado!"));

        return mapToResponseDTO(admin);
    }

    private PlatformAdminResponseDTO mapToResponseDTO(PlatformAdmin admin) {
        return new PlatformAdminResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
