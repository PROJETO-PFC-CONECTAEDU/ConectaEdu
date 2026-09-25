package com.conectaedu.api.modules.user.student.controller;

import com.conectaedu.api.modules.user.student.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.student.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.student.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.student.interfaces.IStudentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class StudentController {

    private final IStudentFacade studentFacade;

    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(@RequestBody @Valid StudentCreationRequestDTO request) {
        StudentResponseDTO response = studentFacade.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid StudentUpdateRequestDTO request) {
        StudentResponseDTO response = studentFacade.updateStudent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentFacade.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> get(@PathVariable UUID id) {
        StudentResponseDTO response = studentFacade.getStudent(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAll() {
        return ResponseEntity.ok(studentFacade.getAllStudents());
    }

    @PatchMapping("/{id}/validate")
    public ResponseEntity<Void> validate(@PathVariable UUID id) {
        studentFacade.validateStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID id) {
        studentFacade.rejectStudent(id);
        return ResponseEntity.noContent().build();
    }
}
