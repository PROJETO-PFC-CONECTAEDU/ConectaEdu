package com.conectaedu.api.modules.demand.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.dto.request.DemandCreationRequestDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.mapper.DemandMapper;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
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
public class DemandCreationService {

    private final DemandRepository demandRepository;
    private final SchoolDirectorRepository schoolDirectorRepository;
    private final AuditService auditService;
    private final DemandMapper demandMapper;

    @Transactional
    public DemandResponseDTO createDemand(DemandCreationRequestDTO request) {
        UUID directorId = SecurityContextHolder.getAuthenticatedUserId();
        SchoolDirector director = schoolDirectorRepository.findById(directorId)
                .orElseThrow(() -> new RuntimeException("Diretor não encontrado"));

        Demand demand = new Demand();
        demand.setTitle(request.title());
        demand.setDescription(request.description());
        demand.setSubject(request.subject());
        demand.setGradeLevel(request.gradeLevel());
        demand.setPupilAmount(request.pupilAmount());
        demand.setClassDate(request.classDate());
        demand.setTotalHours(request.totalHours());
        demand.setRoom(request.room());
        demand.setDifficultyLevel(request.difficultyLevel());
        demand.setStatus(DemandStatus.WAITING);
        demand.setCreatedAt(LocalDateTime.now());
        
        demand.setDirector(director);
        demand.setSchool(director.getSchool());

        demandRepository.save(demand);

        auditService.logCreate(AuditEntityType.DEMAND, demand.getId(), demand.getTitle());

        return demandMapper.mapToResponseDTO(demand);
    }
}
