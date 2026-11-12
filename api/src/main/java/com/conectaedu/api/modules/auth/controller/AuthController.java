package com.conectaedu.api.modules.auth.controller;

import com.conectaedu.api.modules.auth.dto.LoginRequestDTO;
import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.auth.service.AuthLoginService;
import com.conectaedu.api.modules.auth.service.ConfirmEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthLoginService authService;
    private final ConfirmEmailService confirmEmailService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        return ResponseEntity.ok(authService.login(data));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@RequestParam("token") String token) {
        confirmEmailService.confirmEmail(token);
        return ResponseEntity.ok().build();
    }
}
