package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandPublishService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO publish(UUID demandId) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();

        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (!demand.getDirector().getId().equals(directorId)) {
            throw new RuntimeException("Apenas o diretor responsável pode finalizar a agenda");
        }

        if (demand.getStatus() != DemandStatus.DRAFT) {
            throw new RuntimeException("Apenas demandas em rascunho podem ser publicadas");
        }

        var beforeStatus = demand.getStatus();
        demand.setStatus(DemandStatus.WAITING);
        demand.setUpdatedAt(LocalDateTime.now());

        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, demand.getStatus());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
