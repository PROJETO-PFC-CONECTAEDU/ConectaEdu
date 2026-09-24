package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.StudentStatus;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentCreationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final AuditService auditService;

    @Transactional
    public StudentResponseDTO createStudent(StudentCreationRequestDTO request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado!");
        }

        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        if (!university.isActive()) {
            throw new IllegalStateException("A universidade selecionada não está ativa!");
        }

        Student student = new Student();
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPassword(request.password()); // TODO: Implementar criptografia de senha
        student.setUserType(UserType.PERSON);
        student.setUserRole(UserRole.STUDENT);
        student.setCreatedAt(LocalDateTime.now());
        student.setUniversity(university);
        student.setStatus(StudentStatus.PENDING);
        student.setAvailability(request.availability());
        student.setInterestAreas(request.interestAreas());

        studentRepository.save(student);

        auditService.logCreate(AuditEntityType.STUDENT, student.getId(), student.getName());

        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                university.getId(),
                university.getName(),
                student.getStatus(),
                student.getAvailability(),
                student.getInterestAreas()
        );
    }
}
