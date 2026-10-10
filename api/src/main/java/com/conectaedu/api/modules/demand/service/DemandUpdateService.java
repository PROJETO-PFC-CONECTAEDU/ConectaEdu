package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.request.DemandUpdateRequestDTO;
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
public class DemandUpdateService {

    private final DemandRepository demandRepository;
    private final DemandMapper demandMapper;
    private final AuditService auditService;

    @Transactional
    public DemandResponseDTO updateDemand(UUID id, DemandUpdateRequestDTO request) {
        Demand demand = demandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demanda não encontrada"));

        String beforeTitle = demand.getTitle();
        String beforeDescription = demand.getDescription();
        String beforeSubject = demand.getSubject();
        String beforeGradeLevel = demand.getGradeLevel();
        Integer beforePupilAmount = demand.getPupilAmount();
        LocalDateTime beforeClassDate = demand.getClassDate();
        String beforeTotalHours = demand.getTotalHours();
        String beforeRoom = demand.getRoom();
        String beforeDifficultyLevel = demand.getDifficultyLevel();
        var beforeStatus = demand.getStatus();

        if (request.title() != null) demand.setTitle(request.title());
        if (request.description() != null) demand.setDescription(request.description());
        if (request.subject() != null) demand.setSubject(request.subject());
        if (request.gradeLevel() != null) demand.setGradeLevel(request.gradeLevel());
        if (request.pupilAmount() != null) demand.setPupilAmount(request.pupilAmount());
        
        if (request.classDate() != null) {
            if (demand.getStatus() == DemandStatus.ONGOING && !request.classDate().equals(demand.getClassDate())) {
                demand.setPendingClassDate(request.classDate());
            } else {
                demand.setClassDate(request.classDate());
            }
        }

        if (request.totalHours() != null) demand.setTotalHours(request.totalHours());
        
        if (request.room() != null) {
            if (demand.getStatus() == DemandStatus.ONGOING && !request.room().equals(demand.getRoom())) {
                demand.setPendingRoom(request.room());
            } else {
                demand.setRoom(request.room());
            }
        }
        
        if (request.difficultyLevel() != null) demand.setDifficultyLevel(request.difficultyLevel());
        if (request.status() != null) demand.setStatus(request.status());

        demand.setUpdatedAt(LocalDateTime.now());
        demandRepository.save(demand);

        AuditDiff diff = AuditDiff.create()
                .field("titulo", beforeTitle, demand.getTitle())
                .field("descricao", beforeDescription, demand.getDescription())
                .field("disciplina", beforeSubject, demand.getSubject())
                .field("nivelEscolar", beforeGradeLevel, demand.getGradeLevel())
                .field("quantidadeAlunos", beforePupilAmount, demand.getPupilAmount())
                .field("dataAula", beforeClassDate, demand.getClassDate())
                .field("totalHoras", beforeTotalHours, demand.getTotalHours())
                .field("sala", beforeRoom, demand.getRoom())
                .field("grauDificuldade", beforeDifficultyLevel, demand.getDifficultyLevel())
                .field("status", beforeStatus, demand.getStatus());

        auditService.logUpdate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle(), diff);

        return demandMapper.mapToResponseDTO(demand);
    }
}
