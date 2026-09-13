package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentGetService {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public StudentResponseDTO getStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

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
