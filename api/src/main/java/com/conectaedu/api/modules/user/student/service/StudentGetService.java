package com.conectaedu.api.modules.user.student.service;

import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGetService {

    private final StudentRepository studentRepository;
    private final TenancyValidator tenancyValidator;

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents(String search) {
        List<Student> students;
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            students = studentRepository.findAll();
        } else if (SecurityContextHolder.hasRole("UNIVERSITY_ADMIN") || SecurityContextHolder.getAuthenticatedUniversityId() != null) {
            UUID universityId = SecurityContextHolder.getAuthenticatedUniversityId();
            students = studentRepository.findByUniversityId(universityId);
        } else if (SecurityContextHolder.hasRole("SCHOOL_DIRECTOR")) {
            UUID schoolId = SecurityContextHolder.getAuthenticatedSchoolId();
            students = schoolId != null ? studentRepository.findAllBySchoolId(schoolId) : List.of();
        } else {
            return List.of();
        }

        return students.stream()
                .filter(s -> search == null || search.isBlank() ||
                        (s.getName() != null && s.getName().toLowerCase().contains(search.toLowerCase())) ||
                        (s.getEmail() != null && s.getEmail().toLowerCase().contains(search.toLowerCase())))
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudent(UUID id) {
        Student student = studentRepository.findWithInterestAreasById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        tenancyValidator.validateStudentAccess(student);

        return mapToResponseDTO(student);
    }

    private StudentResponseDTO mapToResponseDTO(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getUniversity() != null ? student.getUniversity().getId() : null,
                student.getUniversity() != null ? student.getUniversity().getName() : "Sem Universidade",
                student.getStatus(),
                student.getAvailability(),
                student.getCourse(),
                student.getAddress(),
                student.getLatitude(),
                student.getLongitude(),
                student.getInterestAreas() != null
                        ? new ArrayList<>(student.getInterestAreas())
                        : List.of()
        );
    }
}
