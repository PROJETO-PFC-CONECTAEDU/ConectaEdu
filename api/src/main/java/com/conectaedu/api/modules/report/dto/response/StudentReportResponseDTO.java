package com.conectaedu.api.modules.report.dto.response;

import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StudentReportResponseDTO(
        UUID studentId,
        String studentName,
        LocalDate from,
        LocalDate to,
        UUID schoolId,

        //progresso do estágio.
        BigDecimal totalHours,
        int requiredHours,
        BigDecimal remainingHours,
        LocalDate requirementMetAt,

        //atividades no período.
        int performedLessons,
        BigDecimal validatedHoursInPeriod,
        BigDecimal pendingHoursInPeriod,
        int canceledLessons,
        int notCompletedLessons,

        //avaliações no período.
        Double averageReceived,
        List<StudentEvaluationResponseDTO> evaluations,
        List<ClassProgressPointDTO> progress) {}
