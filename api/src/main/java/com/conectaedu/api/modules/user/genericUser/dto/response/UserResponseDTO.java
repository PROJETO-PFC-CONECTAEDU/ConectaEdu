package com.conectaedu.api.modules.user.genericUser.facade.dto.response;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        UserType userType,
        UserRole userRole
) {
    //  Mapper "manual"
    public UserResponseDTO(User user) {
        this(user.getId(), user.getName(), user.getEmail(), user.getUserType(), user.getUserRole());
    }
}
