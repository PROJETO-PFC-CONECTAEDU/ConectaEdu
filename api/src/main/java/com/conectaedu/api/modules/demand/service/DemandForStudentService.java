package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandForStudentService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getAvailableDemandsForStudents() {
        return demandRepository.findByStatus(DemandStatus.WAITING).stream()
                .map(demandMapper::mapToStudentDTO)
                .collect(Collectors.toList());
    }
}
