package com.conectaedu.api.modules.feedback.controller;

import com.conectaedu.api.modules.feedback.dto.request.ClassEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.request.StudentEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.DemandEvaluationsResponseDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.interfaces.IFeedbackFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//só POST e GET: a RN-50 proíbe editar ou excluir avaliação.

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackFacade feedbackFacade;

    @PostMapping("/student")
    @PreAuthorize("hasRole('SCHOOL_DIRECTOR')")
    public ResponseEntity<StudentEvaluationResponseDTO> evaluateStudent(
            @RequestBody @Valid StudentEvaluationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackFacade.evaluateStudent(request));
    }

    @PostMapping("/class")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClassEvaluationResponseDTO> evaluateClass(
            @RequestBody @Valid ClassEvaluationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackFacade.evaluateClass(request));
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SCHOOL_DIRECTOR', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<StudentEvaluationResponseDTO>> getByStudent(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(feedbackFacade.getEvaluationsByStudent(id, from, to));
    }

    @GetMapping("/demand/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_DIRECTOR', 'PLATFORM_ADMIN')")
    public ResponseEntity<DemandEvaluationsResponseDTO> getByDemand(@PathVariable UUID id) {
        return ResponseEntity.ok(feedbackFacade.getEvaluationsByDemand(id));
    }
}
