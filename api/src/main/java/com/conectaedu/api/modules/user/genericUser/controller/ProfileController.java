package com.conectaedu.api.modules.user.genericUser.controller;

import com.conectaedu.api.modules.user.genericUser.dto.request.ChangePasswordRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.request.ProfileUpdateRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.service.UserProfileService;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody @Valid ProfileUpdateRequestDTO request) {
        UUID userId = SecurityContextHolder.getAuthenticatedUserId();
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequestDTO request) {
        UUID userId = SecurityContextHolder.getAuthenticatedUserId();
        userProfileService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportData() {
        UUID userId = SecurityContextHolder.getAuthenticatedUserId();
        return ResponseEntity.ok(userProfileService.exportUserData(userId));
    }
}
