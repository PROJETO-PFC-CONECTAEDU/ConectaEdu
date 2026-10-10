package com.conectaedu.api.modules.user.university_admin.service;

import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityAdminGetService {

    private final UniversityAdminRepository universityAdminRepository;

    @Transactional(readOnly = true)
    public List<UniversityAdminResponseDTO> getAllUniversityAdmins() {
        return universityAdminRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UniversityAdminResponseDTO getUniversityAdmin(UUID id) {
        UniversityAdmin admin = universityAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador de universidade não encontrado!"));

        return mapToResponseDTO(admin);
    }

    private UniversityAdminResponseDTO mapToResponseDTO(UniversityAdmin admin) {
        return new UniversityAdminResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getUniversity() != null ? admin.getUniversity().getId() : null,
                admin.getUniversity() != null ? admin.getUniversity().getName() : "Sem Universidade",
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
