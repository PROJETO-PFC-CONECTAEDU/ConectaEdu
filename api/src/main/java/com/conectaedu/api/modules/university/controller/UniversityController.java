package com.conectaedu.api.modules.university.controller;

import com.conectaedu.api.modules.university.dto.request.UniversityCreationRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityUpdateRequestDTO;
import com.conectaedu.api.modules.university.dto.request.UniversityValidationRequestDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityCreationResponseDTO;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.interfaces.IUniversityFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final IUniversityFacade universityFacade;

    @PostMapping
    public ResponseEntity<UniversityCreationResponseDTO> createUniversity(
            @RequestBody @Valid UniversityCreationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(universityFacade.createUniversity(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniversityResponseDTO> updateUniversity(
            @PathVariable UUID id, @RequestBody @Valid UniversityUpdateRequestDTO request) {
        return ResponseEntity.ok(universityFacade.updateUniversity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID id) {
        universityFacade.deleteUniversity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityResponseDTO> getUniversityById(@PathVariable UUID id) {
        return ResponseEntity.ok(universityFacade.getUniversityById(id));
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<UniversityResponseDTO> getUniversityByCnpj(@PathVariable String cnpj) {
        return ResponseEntity.ok(universityFacade.getUniversityByCnpj(cnpj));
    }

    @GetMapping
    public ResponseEntity<List<UniversityResponseDTO>> getAllUniversities() {
        return ResponseEntity.ok(universityFacade.getAllUniversities());
    }

    //Aprova a universidade e a torna ativa.
    @PatchMapping("/{id}/validate")
    public ResponseEntity<UniversityResponseDTO> validateUniversity(
            @PathVariable UUID id, @RequestBody @Valid UniversityValidationRequestDTO request) {
        return ResponseEntity.ok(universityFacade.validateUniversity(id, request));
    }

    //Recusa a universidade.
    @PatchMapping("/{id}/reject")
    public ResponseEntity<UniversityResponseDTO> rejectUniversity(
            @PathVariable UUID id, @RequestBody @Valid UniversityValidationRequestDTO request) {
        return ResponseEntity.ok(universityFacade.rejectUniversity(id, request));
    }
}
