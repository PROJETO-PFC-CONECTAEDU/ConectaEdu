package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityListService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public List<UniversityResponseDTO> listAll() {
        if (SecurityContextHolder.hasRole("PLATFORM_ADMIN")) {
            return universityRepository.findAll()
                    .stream()
                    .map(UniversityResponseDTO::new)
                    .toList();
        }

        UUID universityId = SecurityContextHolder.getAuthenticatedUniversityId();
        if (universityId != null) {
            return universityRepository.findById(universityId)
                    .stream()
                    .map(UniversityResponseDTO::new)
                    .toList();
        }

        return List.of();
    }

    @Transactional(readOnly = true)
    public List<UniversityResponseDTO> listActive() {
        return universityRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(UniversityResponseDTO::new)
                .toList();
    }
}

