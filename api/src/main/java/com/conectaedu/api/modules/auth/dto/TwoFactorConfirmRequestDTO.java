package com.conectaedu.api.modules.auth.dto;

import jakarta.validation.constraints.NotNull;

public record TwoFactorConfirmRequestDTO(@NotNull Integer code) {}
