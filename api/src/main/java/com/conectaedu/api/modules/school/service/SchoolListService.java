package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolListService {

    private final SchoolRepository schoolRepository;

    public List<SchoolResponseDTO> ListAll() {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return schoolRepository.findAll()
                    .stream()
                    .map(SchoolResponseDTO::new)
                    .collect(Collectors.toList());
        }

        UUID schoolId = SecurityContextHolder.getAuthenticatedSchoolId();
        if (schoolId != null) {
            return schoolRepository.findById(schoolId)
                    .stream()
                    .map(SchoolResponseDTO::new)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    public List<SchoolResponseDTO> listActive() {
        return schoolRepository.findByActiveTrue()
                .stream()
                .map(SchoolResponseDTO::new)
                .collect(Collectors.toList());
    }
}
