package com.conectaedu.api.modules.user.student.facade;

import com.conectaedu.api.modules.user.student.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.student.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.student.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.student.interfaces.IStudentFacade;
import com.conectaedu.api.modules.user.student.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StudentFacadeImpl implements IStudentFacade {

    private final StudentCreationService studentCreationService;
    private final StudentUpdateService studentUpdateService;
    private final StudentGetService studentGetService;
    private final StudentDeletionService studentDeletionService;
    private final StudentValidationService studentValidationService;

    public StudentResponseDTO createStudent(StudentCreationRequestDTO request) {
        return studentCreationService.createStudent(request);
    }

    public StudentResponseDTO updateStudent(UUID id, StudentUpdateRequestDTO request) {
        return studentUpdateService.updateStudent(id, request);
    }

    public StudentResponseDTO getStudent(UUID id) {
        return studentGetService.getStudent(id);
    }

    @Override
    public List<StudentResponseDTO> getAllStudents() {
        return studentGetService.getAllStudents();
    }

    @Override
    public void deleteStudent(UUID id) {
        studentDeletionService.deleteStudent(id);
    }

    public void validateStudent(UUID id) {
        studentValidationService.validateStudent(id);
    }

    public void rejectStudent(UUID id) {
        studentValidationService.rejectStudent(id);
    }
}
