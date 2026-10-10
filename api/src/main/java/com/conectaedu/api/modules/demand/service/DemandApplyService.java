package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
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
public class DemandApplyService {

    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO apply(UUID demandId) {
        UUID studentId = SecurityContextHolder.getAuthenticatedUserId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (demand.getStatus() != DemandStatus.WAITING) {
            throw new RuntimeException("Esta demanda não está mais disponível para candidaturas");
        }

        if (demand.getStudent() != null) {
            throw new RuntimeException("Esta demanda já possui um estudante vinculado");
        }

        if (demand.getCandidates().contains(student)) {
            throw new RuntimeException("Você já se candidatou a esta demanda");
        }

        demand.getCandidates().add(student);
        demand.setUpdatedAt(LocalDateTime.now());

        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("candidatura", null, student.getName());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
