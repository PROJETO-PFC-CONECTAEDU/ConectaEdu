package com.conectaedu.api.modules.school.dto.request;

public record SchoolUpdateRequestDTO(
        String name,
        String director,
        String address
) {
}
