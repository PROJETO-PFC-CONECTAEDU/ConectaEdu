package com.conectaedu.api.modules.user.controller;

import com.conectaedu.api.modules.user.dto.request.StudentCreationRequestDTO;
import com.conectaedu.api.modules.user.dto.request.StudentUpdateRequestDTO;
import com.conectaedu.api.modules.user.dto.response.StudentResponseDTO;
import com.conectaedu.api.modules.user.interfaces.IStudentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
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

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> get(@PathVariable UUID id) {
        StudentResponseDTO response = studentFacade.getStudent(id);
        return ResponseEntity.ok(response);
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
