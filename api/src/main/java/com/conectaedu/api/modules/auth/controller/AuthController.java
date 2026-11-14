package com.conectaedu.api.modules.auth.controller;

import com.conectaedu.api.modules.auth.dto.*;
import com.conectaedu.api.modules.auth.service.*;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
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
    private final RegisterDirectorService registerDirectorService;
    private final RegisterUniversityAdminService registerUniversityAdminService;
    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        return ResponseEntity.ok(authService.login(data));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<LoginResponseDTO> verify2FA(@RequestBody @Valid TwoFactorVerifyRequestDTO data) {
        return ResponseEntity.ok(twoFactorAuthService.verify2FA(data));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupResponseDTO> setup2FA() {
        User user = twoFactorAuthService.findById(SecurityContextHolder.getAuthenticatedUserId());
        return ResponseEntity.ok(twoFactorAuthService.setup2FA(user));
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<Void> confirm2FA(@RequestBody @Valid TwoFactorConfirmRequestDTO data) {
        User user = twoFactorAuthService.findById(SecurityContextHolder.getAuthenticatedUserId());
        twoFactorAuthService.confirm2FA(user, data.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/disable/request")
    public ResponseEntity<Void> requestDisable2FA() {
        User user = twoFactorAuthService.findById(SecurityContextHolder.getAuthenticatedUserId());
        twoFactorAuthService.requestDisable2FA(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/disable/confirm")
    public ResponseEntity<Void> confirmDisable2FA(@RequestBody @Valid TwoFactorDisableRequestDTO data) {
        User user = twoFactorAuthService.findById(SecurityContextHolder.getAuthenticatedUserId());
        twoFactorAuthService.confirmDisable2FA(user, data.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@RequestBody @Valid ConfirmEmailRequestDTO request) {
        confirmEmailService.confirmEmail(request.email(), request.code());
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

    @PostMapping("/register/director")
    public ResponseEntity<Void> registerDirector(@RequestBody @Valid SchoolDirectorRegisterDTO data) {
        registerDirectorService.register(data);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/university-admin")
    public ResponseEntity<Void> registerUniversityAdmin(@RequestBody @Valid UniversityAdminRegisterDTO data) {
        registerUniversityAdminService.register(data);
        return ResponseEntity.ok().build();
    }
}
