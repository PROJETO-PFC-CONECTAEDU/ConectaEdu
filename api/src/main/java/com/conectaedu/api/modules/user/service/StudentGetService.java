package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGetService {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

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
                student.getInterestAreas()
        );
    }
}
