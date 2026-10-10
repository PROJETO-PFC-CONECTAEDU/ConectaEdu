package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.enums.DemandStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandGetService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;

    @Transactional(readOnly = true)
    public DemandResponseDTO getDemand(UUID id) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));
        return demandMapper.mapToResponseDTO(demand);
    }

    @Transactional(readOnly = true)
    public List<DemandResponseDTO> getAllDemands() {
        return demandRepository.findAll().stream()
                .map(demandMapper::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemandResponseDTO> getDemandsByStatus(DemandStatus status) {
        return demandRepository.findByStatus(status).stream()
                .map(demandMapper::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemandStudentDTO> getDemandsByStatusAndStudentId(DemandStatus status, UUID studentId) {
        return demandRepository.findByStatusAndStudentId(status, studentId).stream()
                .map(demandMapper::mapToStudentDTO)
                .toList();
    }
}
