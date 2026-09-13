package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentDeletionService {

    private final StudentRepository studentRepository;

    @Transactional
    public void deleteStudent(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new UserNotFoundException("Estudante não encontrado!");
        }
        studentRepository.deleteById(id);
    }
}
