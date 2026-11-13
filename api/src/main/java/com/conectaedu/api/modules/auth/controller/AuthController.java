package com.conectaedu.api.modules.auth.controller;

import com.conectaedu.api.modules.auth.dto.ForgotPasswordRequest;
import com.conectaedu.api.modules.auth.dto.LoginRequestDTO;
import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.auth.dto.ResetPasswordRequest;
import com.conectaedu.api.modules.auth.service.AuthLoginService;
import com.conectaedu.api.modules.auth.service.ConfirmEmailService;
import com.conectaedu.api.modules.auth.service.ForgetPasswordService;
import com.conectaedu.api.modules.auth.service.ResetPasswordService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final ForgetPasswordService forgetPasswordService;
    private final ResetPasswordService resetPasswordService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        return ResponseEntity.ok(authService.login(data));
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@RequestParam("token") String token) {
        confirmEmailService.confirmEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        forgetPasswordService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request, HttpServletRequest httpRequest) {
        resetPasswordService.resetPassword(request, httpRequest);
        return ResponseEntity.ok().build();
    }
}
