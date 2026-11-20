package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemandDeletionService {

    private final DemandRepository demandRepository;
    private final AuditService auditService;
    private final TenancyValidator tenancyValidator;

    @Transactional
    public void deleteDemand(UUID id) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new DemandNotFoundException("Demanda não encontrada"));

        tenancyValidator.validateDemandAccess(demand);

        demand.anonymize();
        demandRepository.save(demand);

        auditService.logDelete(AuditEntityType.DEMAND, demand.getId(), "Demanda Anonimizada");
    }
}
