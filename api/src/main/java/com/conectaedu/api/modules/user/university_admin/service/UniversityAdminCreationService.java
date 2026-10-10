package com.conectaedu.api.modules.user.university_admin.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UniversityAdminCreationService {

    private final UserRepository userRepository;
    private final UniversityAdminRepository universityAdminRepository;
    private final UniversityRepository universityRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UniversityAdminResponseDTO createUniversityAdmin(UniversityAdminCreationRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado!");
        }

        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        if (!university.isActive()) {
            throw new IllegalStateException("A universidade selecionada não está ativa!");
        }

        UniversityAdmin admin = UniversityAdmin.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .userType(UserType.PERSON)
                .userRole(UserRole.UNIVERSITY_ADMIN)
                .createdAt(LocalDateTime.now())
                .university(university)
                .build();

        universityAdminRepository.save(admin);

        auditService.logCreate(AuditEntityType.UNIVERSITY_ADMIN, admin.getId(), admin.getName());

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
