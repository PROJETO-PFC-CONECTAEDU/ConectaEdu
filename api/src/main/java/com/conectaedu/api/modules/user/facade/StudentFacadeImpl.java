package com.conectaedu.api.modules.user.facade;

import com.conectaedu.api.modules.user.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.interfaces.IStudentFacade;
import com.conectaedu.api.modules.user.service.StudentCreationService;
import com.conectaedu.api.modules.user.service.StudentGetService;
import com.conectaedu.api.modules.user.service.StudentUpdateService;
import com.conectaedu.api.modules.user.service.StudentValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StudentFacadeImpl implements IStudentFacade {

    private final StudentCreationService studentCreationService;
    private final StudentUpdateService studentUpdateService;
    private final StudentGetService studentGetService;
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

    public void validateStudent(UUID id) {
        studentValidationService.validateStudent(id);
    }

    public void rejectStudent(UUID id) {
        studentValidationService.rejectStudent(id);
    }
}
