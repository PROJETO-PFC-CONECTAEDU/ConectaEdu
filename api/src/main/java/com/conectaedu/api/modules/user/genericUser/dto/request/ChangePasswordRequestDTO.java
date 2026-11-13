package com.conectaedu.api.modules.user.genericUser.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
    @NotBlank(message = "Senha atual é obrigatória")
    String currentPassword,

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres")
    String newPassword
) {
}
