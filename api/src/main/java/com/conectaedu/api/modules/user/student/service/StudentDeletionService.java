package com.conectaedu.api.modules.user.student.service;

import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentDeletionService {

    private final StudentRepository studentRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        tenancyValidator.validateStudentAccess(student);

        studentRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.STUDENT, student.getId(), student.getName());
    }
}
