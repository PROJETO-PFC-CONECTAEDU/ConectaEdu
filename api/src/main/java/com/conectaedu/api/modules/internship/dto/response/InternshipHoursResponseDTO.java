package com.conectaedu.api.modules.internship.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

//Todos saem da soma das aulas no momento da consulta.

public record InternshipHoursResponseDTO(
        UUID studentId,
        String studentName,
        BigDecimal totalHours,
        int validatedLessons,
        int pendingLessons,
        int canceledLessons,
        int requiredHours,
        BigDecimal remainingHours,
        LocalDate requirementMetAt) {}