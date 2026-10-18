package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.security.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandConfirmChangeService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO confirmChange(UUID demandId) {
        UUID studentId = SecurityContextHolder.getAuthenticatedUserId();
        
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (demand.getStudent() == null || !demand.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Apenas o estudante vinculado pode confirmar alterações");
        }

        if (demand.getPendingClassDate() == null && demand.getPendingRoom() == null) {
            throw new RuntimeException("Não há alterações pendentes para confirmar");
        }

        AuditDiff diff = AuditDiff.create();

        if (demand.getPendingClassDate() != null) {
            diff.field("dataAula", demand.getClassDate(), demand.getPendingClassDate());
            demand.setClassDate(demand.getPendingClassDate());
            demand.setPendingClassDate(null);
        }

        if (demand.getPendingRoom() != null) {
            diff.field("sala", demand.getRoom(), demand.getPendingRoom());
            demand.setRoom(demand.getPendingRoom());
            demand.setPendingRoom(null);
        }

        demand.setUpdatedAt(LocalDateTime.now());
        demandRepository.save(demand);

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
