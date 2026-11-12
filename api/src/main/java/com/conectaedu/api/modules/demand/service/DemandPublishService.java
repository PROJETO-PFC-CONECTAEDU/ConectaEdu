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
import com.conectaedu.api.shared.security.util.TenancyValidator;
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
    private final TenancyValidator tenancyValidator;

    @Transactional
    public DemandResponseDTO publish(UUID demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        tenancyValidator.validateDemandAccess(demand);

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
