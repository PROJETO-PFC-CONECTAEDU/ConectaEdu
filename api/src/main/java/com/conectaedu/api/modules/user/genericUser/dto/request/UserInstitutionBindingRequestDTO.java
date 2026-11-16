package com.conectaedu.api.modules.user.genericUser.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserInstitutionBindingRequestDTO(
    @NotNull(message = "O ID da instituição é obrigatório!")
    UUID institutionId
) {
}
