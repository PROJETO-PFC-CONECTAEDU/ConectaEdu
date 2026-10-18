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
public class DemandUnlinkStudentService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO unlink(UUID demandId) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (demand.getStudent() == null) {
            throw new RuntimeException("Não há estudante vinculado a esta demanda");
        }

        var beforeStatus = demand.getStatus();
        var studentName = demand.getStudent().getName();

        demand.setStudent(null);
        demand.setStatus(DemandStatus.WAITING);
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
                .field("estudante_desvinculado", studentName, null)
                .field("status", beforeStatus, demand.getStatus());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
