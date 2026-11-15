package com.conectaedu.api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TwoFactorVerifyRequestDTO(
    @NotBlank @Email String email,
    @NotNull Integer code
) {}
