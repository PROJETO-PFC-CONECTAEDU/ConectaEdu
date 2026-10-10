package com.conectaedu.api.modules.user.school_director.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorUpdateRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolDirectorUpdateService {

    private final UserRepository userRepository;
    private final SchoolDirectorRepository schoolDirectorRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    @Transactional
    public SchoolDirectorResponseDTO updateSchoolDirector(UUID id, SchoolDirectorUpdateRequestDTO request) {
        SchoolDirector director = schoolDirectorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Diretor de escola não encontrado!"));

        String beforeEmail = director.getEmail();
        String beforeName = director.getName();
        UUID beforeSchoolId = director.getSchool() != null ? director.getSchool().getId() : null;

        if (request.email() != null && !request.email().trim().equalsIgnoreCase(director.getEmail())) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException("Email já cadastrado!");
            }
            director.setEmail(email);
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            director.setName(request.name().trim());
        }

        if (request.schoolId() != null && (director.getSchool() == null || !request.schoolId().equals(director.getSchool().getId()))) {
            School school = schoolRepository.findById(request.schoolId())
                    .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada!"));
            
            if (!school.isActive()) {
                throw new IllegalStateException("A escola selecionada não está ativa!");
            }
            director.setSchool(school);
        }

        director.setUpdatedAt(LocalDateTime.now());
        schoolDirectorRepository.save(director);

        AuditDiff diff = AuditDiff.create()
                .field("email", beforeEmail, director.getEmail())
                .field("name", beforeName, director.getName())
                .field("schoolId", beforeSchoolId, director.getSchool() != null ? director.getSchool().getId() : null);
        auditService.logUpdate(AuditEntityType.SCHOOL_DIRECTOR, director.getId(), director.getName(), diff);

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
