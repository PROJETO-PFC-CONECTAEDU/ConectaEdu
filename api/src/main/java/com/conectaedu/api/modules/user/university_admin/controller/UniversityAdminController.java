package com.conectaedu.api.modules.user.university_admin.controller;

import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.request.UniversityAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.university_admin.dto.response.UniversityAdminResponseDTO;
import com.conectaedu.api.modules.user.university_admin.interfaces.IUniversityAdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/university-admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class UniversityAdminController {

    private final IUniversityAdminFacade universityAdminFacade;

    @PostMapping
    public ResponseEntity<UniversityAdminResponseDTO> create(@RequestBody @Valid UniversityAdminCreationRequestDTO request) {
        UniversityAdminResponseDTO response = universityAdminFacade.createUniversityAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniversityAdminResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid UniversityAdminUpdateRequestDTO request) {
        UniversityAdminResponseDTO response = universityAdminFacade.updateUniversityAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        universityAdminFacade.deleteUniversityAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityAdminResponseDTO> get(@PathVariable UUID id) {
        UniversityAdminResponseDTO response = universityAdminFacade.getUniversityAdmin(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UniversityAdminResponseDTO>> getAll() {
        return ResponseEntity.ok(universityAdminFacade.getAllUniversityAdmins());
    }
}
