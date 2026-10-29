package com.conectaedu.api.modules.dashboard.controller;

import com.conectaedu.api.modules.dashboard.dto.DashboardStatsDTO;
import com.conectaedu.api.modules.dashboard.service.DashboardService;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        var userId = SecurityContextHolder.getAuthenticatedUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + userId));
        return ResponseEntity.ok(dashboardService.getStats(user));
    }
}
