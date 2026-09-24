package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentDeletionService {

    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        studentRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.STUDENT, student.getId(), student.getName());
    }
}
