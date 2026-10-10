package com.conectaedu.api.modules.demand.facade;

import com.conectaedu.api.modules.demand.dto.request.DemandCreationRequestDTO;
import com.conectaedu.api.modules.demand.dto.request.DemandUpdateRequestDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandResponseDTO;
import com.conectaedu.api.modules.demand.dto.response.DemandStudentDTO;
import com.conectaedu.api.modules.demand.interfaces.IDemandFacade;
import com.conectaedu.api.modules.demand.service.*;
import com.conectaedu.api.shared.enums.DemandStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DemandFacadeImpl implements IDemandFacade {

    private final DemandCreationService creationService;
    private final DemandUpdateService updateService;
    private final DemandDeletionService deletionService;
    private final DemandGetService getService;
    private final DemandApplyService applyService;
    private final DemandApproveService approveService;
    private final DemandConfirmChangeService confirmChangeService;
    private final DemandUnlinkStudentService unlinkStudentService;
    private final DemandFinishService finishService;
    private final DemandForStudentService demandForStudentService;

    @Override
    public DemandResponseDTO createDemand(DemandCreationRequestDTO request) {
        return creationService.createDemand(request);
    }

    @Override
    public DemandResponseDTO updateDemand(UUID id, DemandUpdateRequestDTO request) {
        return updateService.updateDemand(id, request);
    }

    @Override
    public void deleteDemand(UUID id) {
        deletionService.deleteDemand(id);
    }

    @Override
    public DemandResponseDTO getDemand(UUID id) {
        return getService.getDemand(id);
    }

    @Override
    public List<DemandResponseDTO> getAllDemands() {
        return getService.getAllDemands();
    }

    @Override
    public List<DemandResponseDTO> getDemandsByStatus(DemandStatus status) {
        return getService.getDemandsByStatus(status);
    }

    @Override
    public List<DemandStudentDTO> getAvailableDemandsForStudents() {
        return demandForStudentService.getAvailableDemandsForStudents();
    }

    @Override
    public DemandResponseDTO applyToDemand(UUID id) {
        return applyService.apply(id);
    }

    @Override
    public DemandResponseDTO approveCandidate(UUID demandId, UUID studentId) {
        return approveService.approve(demandId, studentId);
    }

    @Override
    public DemandResponseDTO confirmChange(UUID demandId) {
        return confirmChangeService.confirmChange(demandId);
    }

    @Override
    public DemandResponseDTO unlinkStudent(UUID demandId) {
        return unlinkStudentService.unlink(demandId);
    }

    @Override
    public DemandResponseDTO finishDemand(UUID demandId) {
        return finishService.finish(demandId);
    }

    @Override
    public List<DemandStudentDTO> getDemandsByStatusAndStudentId(DemandStatus status, UUID studentId) {
        return getService.getDemandsByStatusAndStudentId(status, studentId);
    }
}
