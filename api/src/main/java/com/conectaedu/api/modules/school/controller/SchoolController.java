package com.conectaedu.api.modules.school.controller;

import com.conectaedu.api.modules.school.dto.request.SchoolCreationRequestDTO;
import com.conectaedu.api.modules.school.dto.request.SchoolUpdateRequestDTO;
import com.conectaedu.api.modules.school.dto.request.SchoolValidationRequestDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolCreationResponseDTO;
import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.interfaces.ISchoolFacade;
import com.conectaedu.api.modules.school.service.SchoolValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {
     private final ISchoolFacade  schoolFacade;

     @PostMapping
     @PreAuthorize("hasRole('PLATFORM_ADMIN')")
     public ResponseEntity<SchoolCreationResponseDTO> createSchool(@RequestBody SchoolCreationRequestDTO request) {
         return ResponseEntity.status(HttpStatus.CREATED).body(schoolFacade.createSchool(request));
     }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<SchoolResponseDTO> updateSchool(@PathVariable UUID id, @RequestBody @Valid SchoolUpdateRequestDTO request) {
         return ResponseEntity.ok(schoolFacade.updateSchool(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Void> deleteSchool(@PathVariable UUID id) {
         schoolFacade.deleteSchool(id);
         return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolResponseDTO> getSchoolById(@PathVariable UUID id) {
         return ResponseEntity.ok(schoolFacade.getSchoolById(id));
    }

    @GetMapping("/cie/{cie}")
    public ResponseEntity<SchoolResponseDTO>  getSchoolByCie(@PathVariable String cie) {
         return ResponseEntity.ok(schoolFacade.getSchoolByCie(cie));
    }

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<List<SchoolResponseDTO>> getAllSchools() {return ResponseEntity.ok(schoolFacade.getAllSchools()); }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SCHOOL_DIRECTOR', 'STUDENT')")
    public ResponseEntity<List<SchoolResponseDTO>> getActiveSchools() {
        return ResponseEntity.ok(schoolFacade.listActiveSchools());
    }

    //Ativa a escola.
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<SchoolResponseDTO> activateSchool(
            @PathVariable UUID id, @RequestBody @Valid SchoolValidationRequestDTO request) {
         return ResponseEntity.ok(schoolFacade.activateSchool(id, request));
    }

    //Inativa a escola.
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<SchoolResponseDTO> deactivateSchool(
            @PathVariable UUID id, @RequestBody @Valid SchoolValidationRequestDTO request) {
         return ResponseEntity.ok(schoolFacade.deactivateSchool(id, request));
    }
}
