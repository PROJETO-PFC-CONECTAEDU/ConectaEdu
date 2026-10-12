package com.conectaedu.api.modules.school.service;

import com.conectaedu.api.modules.school.dto.response.SchoolResponseDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolListService {

    private final SchoolRepository schoolRepository;

    public List<SchoolResponseDTO> ListAll() {
        return schoolRepository.findAll()
                .stream()
                .map(SchoolResponseDTO::new)
                .collect(Collectors.toList());
    }
}
