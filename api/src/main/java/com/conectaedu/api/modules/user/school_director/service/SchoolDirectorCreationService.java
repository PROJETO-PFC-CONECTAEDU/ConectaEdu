package com.conectaedu.api.modules.user.school_director.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorCreationRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SchoolDirectorCreationService {

    private final UserRepository userRepository;
    private final SchoolDirectorRepository schoolDirectorRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SchoolDirectorResponseDTO createSchoolDirector(SchoolDirectorCreationRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado!");
        }

        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));

        if (!school.isActive()) {
            throw new IllegalStateException("A escola selecionada não está ativa!");
        }

        SchoolDirector director = SchoolDirector.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .userType(UserType.PERSON)
                .userRole(UserRole.SCHOOL_DIRECTOR)
                .createdAt(LocalDateTime.now())
                .school(school)
                .build();

        schoolDirectorRepository.save(director);

        auditService.logCreate(AuditEntityType.SCHOOL_DIRECTOR, director.getId(), director.getName());

        return mapToResponseDTO(director);
    }

    private SchoolDirectorResponseDTO mapToResponseDTO(SchoolDirector director) {
        return new SchoolDirectorResponseDTO(
                director.getId(),
                director.getName(),
                director.getEmail(),
                director.getSchool().getId(),
                director.getSchool().getName(),
                director.getCreatedAt(),
                director.getUpdatedAt()
        );
    }
}
