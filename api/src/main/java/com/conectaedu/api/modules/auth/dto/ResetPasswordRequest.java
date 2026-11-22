package com.conectaedu.api.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "O e-mail é obrigatório.")
    String email,

    @NotBlank(message = "O código é obrigatório.")
    @Size(min = 6, max = 6, message = "O código deve ter 6 dígitos.")
    String code,
    
    @NotBlank(message = "A nova senha é obrigatória.")
    String newPassword
) {
}
