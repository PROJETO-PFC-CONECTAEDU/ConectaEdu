package com.conectaedu.api.modules.user.school_director.controller;

import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorCreationRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.request.SchoolDirectorUpdateRequestDTO;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;
import com.conectaedu.api.modules.user.school_director.interfaces.ISchoolDirectorFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school-directors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class SchoolDirectorController {

    private final ISchoolDirectorFacade schoolDirectorFacade;

    @PostMapping
    public ResponseEntity<SchoolDirectorResponseDTO> create(@RequestBody @Valid SchoolDirectorCreationRequestDTO request) {
        SchoolDirectorResponseDTO response = schoolDirectorFacade.createSchoolDirector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolDirectorResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid SchoolDirectorUpdateRequestDTO request) {
        SchoolDirectorResponseDTO response = schoolDirectorFacade.updateSchoolDirector(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        schoolDirectorFacade.deleteSchoolDirector(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolDirectorResponseDTO> get(@PathVariable UUID id) {
        SchoolDirectorResponseDTO response = schoolDirectorFacade.getSchoolDirector(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SchoolDirectorResponseDTO>> getAll() {
        return ResponseEntity.ok(schoolDirectorFacade.getAllSchoolDirectors());
    }
}
