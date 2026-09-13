package com.conectaedu.api.modules.user.dto.response;

import com.conectaedu.api.shared.enums.StudentStatus;

import java.util.List;
import java.util.UUID;

public record StudentResponseDTO(
        UUID id,
        String name,
        String email,
        UUID universityId,
        String universityName,
        StudentStatus status,
        String availability,
        List<String> interestAreas
) {
}
