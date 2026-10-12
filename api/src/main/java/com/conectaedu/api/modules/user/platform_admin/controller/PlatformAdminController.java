package com.conectaedu.api.modules.user.platform_admin.controller;

import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminCreationRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.request.PlatformAdminUpdateRequestDTO;
import com.conectaedu.api.modules.user.platform_admin.dto.response.PlatformAdminResponseDTO;
import com.conectaedu.api.modules.user.platform_admin.interfaces.IPlatformAdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform-admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlatformAdminController {

    private final IPlatformAdminFacade platformAdminFacade;

    @PostMapping
    public ResponseEntity<PlatformAdminResponseDTO> create(@RequestBody @Valid PlatformAdminCreationRequestDTO request) {
        PlatformAdminResponseDTO response = platformAdminFacade.createPlatformAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlatformAdminResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid PlatformAdminUpdateRequestDTO request) {
        PlatformAdminResponseDTO response = platformAdminFacade.updatePlatformAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        platformAdminFacade.deletePlatformAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatformAdminResponseDTO> get(@PathVariable UUID id) {
        PlatformAdminResponseDTO response = platformAdminFacade.getPlatformAdmin(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlatformAdminResponseDTO>> getAll() {
        return ResponseEntity.ok(platformAdminFacade.getAllPlatformAdmins());
    }
}
