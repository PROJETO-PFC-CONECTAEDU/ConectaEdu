package com.conectaedu.api.modules.demand.controller;

import com.conectaedu.api.modules.demand.dto.request.DemandCreationRequestDTO;
import com.conectaedu.api.modules.demand.dto.request.DemandUpdateRequestDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.interfaces.IDemandFacade;
import com.conectaedu.api.shared.enums.DemandStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demands")
@RequiredArgsConstructor
public class DemandController {

    private final IDemandFacade demandFacade;

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<DemandResponseDTO> create(@RequestBody @Valid DemandCreationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandFacade.createDemand(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<DemandResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid DemandUpdateRequestDTO request) {
        return ResponseEntity.ok(demandFacade.updateDemand(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        demandFacade.deleteDemand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SCHOOL_DIRECTOR', 'STUDENT')")
    public ResponseEntity<DemandResponseDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(demandFacade.getDemand(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SCHOOL_DIRECTOR')")
    public ResponseEntity<List<DemandResponseDTO>> getAll() {
        return ResponseEntity.ok(demandFacade.getAllDemands());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SCHOOL_DIRECTOR', 'STUDENT')")
    public ResponseEntity<List<DemandResponseDTO>> getAvailableDemands() {
        return ResponseEntity.ok(demandFacade.getDemandsByStatus(DemandStatus.WAITING));
    }

    @GetMapping("/available/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DemandStudentDTO>> getAvailableDemandsForStudents() {
        return ResponseEntity.ok(demandFacade.getAvailableDemandsForStudents());
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DemandResponseDTO> apply(@PathVariable UUID id) {
        return ResponseEntity.ok(demandFacade.applyToDemand(id));
    }

    @PostMapping("/{id}/approve/{studentId}")
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<DemandResponseDTO> approve(@PathVariable UUID id, @PathVariable UUID studentId) {
        return ResponseEntity.ok(demandFacade.approveCandidate(id, studentId));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DemandResponseDTO> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(demandFacade.confirmChange(id));
    }

    @PostMapping("/{id}/unlink")
    public ResponseEntity<DemandResponseDTO> unlink(@PathVariable UUID id) {
        return ResponseEntity.ok(demandFacade.unlinkStudent(id));
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<DemandResponseDTO> finish(@PathVariable UUID id) {
        return ResponseEntity.ok(demandFacade.finishDemand(id));
    }

    @GetMapping("/ongoing")
    @PreAuthorize("hasAnyRole('STUDENT', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<DemandStudentDTO>> getDemandsByStatusAndStudentId(@RequestParam(required = false) UUID studentId) {
        UUID targetStudentId = studentId;
        if (targetStudentId == null) {
            targetStudentId = com.conectaedu.api.shared.security.context.SecurityContextHolder.getAuthenticatedUserId();
        }
        return ResponseEntity.ok(demandFacade.getDemandsByStatusAndStudentId(DemandStatus.ONGOING, targetStudentId));
    }
}
