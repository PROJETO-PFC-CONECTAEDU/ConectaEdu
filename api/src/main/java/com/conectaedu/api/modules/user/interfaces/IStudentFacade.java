package com.conectaedu.api.modules.user.interfaces;

import com.conectaedu.api.modules.user.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IStudentFacade {
    StudentResponseDTO createStudent(StudentCreationRequestDTO request);
    StudentResponseDTO updateStudent(UUID id, StudentUpdateRequestDTO request);
    StudentResponseDTO getStudent(UUID id);
    List<StudentResponseDTO> getAllStudents();
    void deleteStudent(UUID id);
    void validateStudent(UUID id);
    void rejectStudent(UUID id);
}
