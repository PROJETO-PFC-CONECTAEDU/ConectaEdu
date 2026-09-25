package com.conectaedu.api.modules.user.student.interfaces;

import com.conectaedu.api.modules.user.student.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.student.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.student.dto.response.StudentResponseDTO;

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
