package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.domain.DemandStudent;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
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
public class DemandFinishService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO finish(UUID demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (demand.getStatus() != DemandStatus.ONGOING) {
            throw new RuntimeException("Apenas demandas em reforço podem ser concluídas");
        }

        var beforeStatus = demand.getStatus();
        var beforeArchived = demand.isArchived();

        demand.setStatus(DemandStatus.ARCHIVED);
        demand.setArchived(true);
        demand.setUpdatedAt(LocalDateTime.now());

        // Update history
        demand.getStudentHistory().stream()
                .filter(DemandStudent::isActive)
                .forEach(h -> {
                    h.setActive(false);
                    h.setUnlinkedAt(LocalDateTime.now());
                });

        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("status", beforeStatus, demand.getStatus())
                .field("arquivado", beforeArchived, demand.isArchived());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
