package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.Student;
import com.conectaedu.api.modules.user.repository.StudentRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.StudentStatus;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentValidationService {

    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public void validateStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        StudentStatus beforeStatus = student.getStatus();

        student.setValidatedAt(LocalDateTime.now());
        student.setStatus(StudentStatus.VALIDATED);
        studentRepository.save(student);

        AuditDiff diff = AuditDiff.create().field("status", beforeStatus, student.getStatus());
        auditService.logUpdate(AuditEntityType.STUDENT, student.getId(), student.getName(), diff);
    }

    @Transactional
    public void rejectStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Estudante não encontrado!"));

        StudentStatus beforeStatus = student.getStatus();

        student.setStatus(StudentStatus.REJECTED);
        studentRepository.save(student);

        AuditDiff diff = AuditDiff.create().field("status", beforeStatus, student.getStatus());
        auditService.logUpdate(AuditEntityType.STUDENT, student.getId(), student.getName(), diff);
    }

}
