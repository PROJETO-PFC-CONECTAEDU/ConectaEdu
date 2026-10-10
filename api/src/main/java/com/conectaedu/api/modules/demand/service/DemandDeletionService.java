package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.DemandStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandDeletionService {

    private final DemandRepository demandRepository;
    private final AuditService auditService;

    @Transactional
    public void deleteDemand(UUID id) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        var beforeStatus = demand.getStatus();
        var beforeArchived = demand.isArchived();

        demand.setArchived(true);
        demand.setStatus(DemandStatus.ARCHIVED);
        demand.setUpdatedAt(LocalDateTime.now());
        
        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, demand.getStatus())
                .field("arquivado", beforeArchived, demand.isArchived());
        
        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);
    }
}
