package com.conectaedu.api.modules.user.university_admin.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityAdminUpdateService {

    private final UserRepository userRepository;
    private final UniversityAdminRepository universityAdminRepository;
    private final UniversityRepository universityRepository;
    private final AuditService auditService;

    @Transactional
    public UniversityAdminResponseDTO updateUniversityAdmin(UUID id, UniversityAdminUpdateRequestDTO request) {
        UniversityAdmin admin = universityAdminRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Administrador de universidade não encontrado!"));

        String beforeEmail = admin.getEmail();
        String beforeName = admin.getName();
        UUID beforeUniversityId = admin.getUniversity() != null ? admin.getUniversity().getId() : null;

        if (request.email() != null && !request.email().trim().equalsIgnoreCase(admin.getEmail())) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException("Email já cadastrado!");
            }
            admin.setEmail(email);
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            admin.setName(request.name().trim());
        }

        if (request.universityId() != null && (admin.getUniversity() == null || !request.universityId().equals(admin.getUniversity().getId()))) {
            University university = universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));
            
            if (!university.isActive()) {
                throw new IllegalStateException("A universidade selecionada não está ativa!");
            }
            admin.setUniversity(university);
        }

        admin.setUpdatedAt(LocalDateTime.now());
        universityAdminRepository.save(admin);

        AuditDiff diff = AuditDiff.create()
                .field("email", beforeEmail, admin.getEmail())
                .field("name", beforeName, admin.getName())
                .field("universityId", beforeUniversityId, admin.getUniversity() != null ? admin.getUniversity().getId() : null);
        auditService.logUpdate(AuditEntityType.UNIVERSITY_ADMIN, admin.getId(), admin.getName(), diff);

        return mapToResponseDTO(admin);
    }

    private UniversityAdminResponseDTO mapToResponseDTO(UniversityAdmin admin) {
        return new UniversityAdminResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getUniversity().getId(),
                admin.getUniversity().getName(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }
}
