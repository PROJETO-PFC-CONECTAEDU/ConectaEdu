package com.conectaedu.api.modules.user.school_director.service;

import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.dto.response.SchoolDirectorResponseDTO;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolDirectorGetService {

    private final SchoolDirectorRepository schoolDirectorRepository;

    @Transactional(readOnly = true)
    public List<SchoolDirectorResponseDTO> getAllSchoolDirectors() {
        return schoolDirectorRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SchoolDirectorResponseDTO getSchoolDirector(UUID id) {
        SchoolDirector director = schoolDirectorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Diretor de escola não encontrado!"));

        return mapToResponseDTO(director);
    }

    private SchoolDirectorResponseDTO mapToResponseDTO(SchoolDirector director) {
        return new SchoolDirectorResponseDTO(
                director.getId(),
                director.getName(),
                director.getEmail(),
                director.getSchool() != null ? director.getSchool().getId() : null,
                director.getSchool() != null ? director.getSchool().getName() : "Sem Escola",
                director.getCreatedAt(),
                director.getUpdatedAt()
        );
    }
}
