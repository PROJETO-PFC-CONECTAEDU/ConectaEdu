package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.enums.StudentStatus;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentValidationService {

    private final StudentRepository studentRepository;

    @Transactional
    public void validateStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        student.setStatus(StudentStatus.VALIDATED);
        studentRepository.save(student);
    }

    @Transactional
    public void rejectStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        student.setStatus(StudentStatus.REJECTED);
        studentRepository.save(student);
    }

}
