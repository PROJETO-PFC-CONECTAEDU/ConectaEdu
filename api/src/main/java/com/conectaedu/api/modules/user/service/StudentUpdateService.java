package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.modules.user.repository.UserRepository;
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
public class StudentUpdateService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final AuditService auditService;

    @Transactional
    public StudentResponseDTO updateStudent(UUID id, StudentUpdateRequestDTO request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        String beforeEmail = student.getEmail();
        String beforeName = student.getName();
        var beforeUserType = student.getUserType();
        var beforeUniversityId = student.getUniversity() != null ? student.getUniversity().getId() : null;
        String beforeAvailability = student.getAvailability();
        var beforeInterestAreas = student.getInterestAreas();

        if (request.email() != null && !request.email().trim().equalsIgnoreCase(student.getEmail())) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyExistsException("Email já cadastrado!");
            }
            student.setEmail(email);
        }

        if (request.name() != null && !request.name().trim().isEmpty()) {
            student.setName(request.name().trim());
        }

        if (request.userType() != null) {
            student.setUserType(request.userType());
        }

        if (request.universityId() != null) {
            University university = universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));
            
            if (!university.isActive()) {
                throw new IllegalStateException("A universidade selecionada não está ativa!");
            }
            student.setUniversity(university);
        }

        if (request.availability() != null) {
            student.setAvailability(request.availability());
        }

        if (request.interestAreas() != null) {
            student.setInterestAreas(request.interestAreas());
        }

        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);

        var afterUniversityId = student.getUniversity() != null ? student.getUniversity().getId() : null;
        AuditDiff diff = AuditDiff.create()
                .field("email", beforeEmail, student.getEmail())
                .field("name", beforeName, student.getName())
                .field("userType", beforeUserType, student.getUserType())
                .field("universityId", beforeUniversityId, afterUniversityId)
                .field("availability", beforeAvailability, student.getAvailability())
                .field("interestAreas", beforeInterestAreas, student.getInterestAreas());
        auditService.logUpdate(AuditEntityType.STUDENT, student.getId(), student.getName(), diff);

        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getUniversity().getId(),
                student.getUniversity().getName(),
                student.getStatus(),
                student.getAvailability(),
                student.getInterestAreas()
        );
    }
}
