package com.conectaedu.api.modules.feedback.interfaces;

import com.conectaedu.api.modules.feedback.dto.request.ClassEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.request.StudentEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.DemandEvaluationsResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IFeedbackFacade {

    StudentEvaluationResponseDTO evaluateStudent(StudentEvaluationRequestDTO request);

    ClassEvaluationResponseDTO evaluateClass(ClassEvaluationRequestDTO request);

    List<StudentEvaluationResponseDTO> getEvaluationsByStudent(UUID studentId, LocalDateTime from, LocalDateTime to);

    DemandEvaluationsResponseDTO getEvaluationsByDemand(UUID demandId);
}
