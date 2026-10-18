package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.DemandStudent;
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
public class DemandApproveService {

    private final DemandRepository demandRepository;
    private final StudentRepository studentRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO approve(UUID demandId, UUID studentId) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();

        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        if (!demand.getDirector().getId().equals(directorId)) {
            throw new RuntimeException("Apenas o diretor responsável pode aprovar candidatos");
        }

        if (demand.getStatus() != DemandStatus.WAITING) {
            throw new RuntimeException("Esta demanda não está em espera");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        if (!demand.getCandidates().contains(student)) {
            throw new RuntimeException("Este estudante não se candidatou a esta demanda");
        }

        var beforeStatus = demand.getStatus();
        
        demand.setStudent(student);
        demand.setStatus(DemandStatus.ONGOING);
        demand.setUpdatedAt(LocalDateTime.now());

        DemandStudent history = new DemandStudent();
        history.setDemand(demand);
        history.setStudent(student);
        history.setActive(true);
        history.setLinkedAt(LocalDateTime.now());
        demand.getStudentHistory().add(history);
        
        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("estudante_aprovado", null, student.getName())
                .field("status", beforeStatus, demand.getStatus());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
