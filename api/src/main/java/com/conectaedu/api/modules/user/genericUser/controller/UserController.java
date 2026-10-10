package com.conectaedu.api.modules.user.genericUser.controller;

import com.conectaedu.api.modules.user.genericUser.facade.dto.request.UserCreationRequestDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.request.UserUpdateRequestDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserCreationResponseDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.interfaces.IUserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class UserController {

    private final IUserFacade userFacade;

    @PostMapping
    public ResponseEntity<UserCreationResponseDTO> createUser(@RequestBody @Valid UserCreationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userFacade.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody @Valid UserUpdateRequestDTO request) {
        return ResponseEntity.ok(userFacade.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userFacade.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userFacade.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userFacade.getUserByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userFacade.getAllUsers());
    }
}
