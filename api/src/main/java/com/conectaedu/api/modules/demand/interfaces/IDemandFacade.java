package com.conectaedu.api.modules.demand.interfaces;

import com.conectaedu.api.modules.demand.dto.request.DemandCreationRequestDTO;
import com.conectaedu.api.modules.demand.dto.request.DemandUpdateRequestDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.shared.enums.DemandStatus;

import java.util.List;
import java.util.UUID;

public interface IDemandFacade {
    DemandResponseDTO createDemand(DemandCreationRequestDTO request);
    DemandResponseDTO updateDemand(UUID id, DemandUpdateRequestDTO request);
    void deleteDemand(UUID id);
    DemandResponseDTO getDemand(UUID id);
    List<DemandResponseDTO> getAllDemands();
    List<DemandResponseDTO> getDemandsByStatus(DemandStatus status);
    List<DemandStudentDTO> getAvailableDemandsForStudents();
    DemandResponseDTO applyToDemand(UUID id);
    DemandResponseDTO approveCandidate(UUID demandId, UUID studentId);
    DemandResponseDTO confirmChange(UUID demandId);
    DemandResponseDTO unlinkStudent(UUID demandId);
    DemandResponseDTO finishDemand(UUID demandId);

    List<DemandStudentDTO> getDemandsByStatusAndStudentId(DemandStatus status, UUID studentId);
}
