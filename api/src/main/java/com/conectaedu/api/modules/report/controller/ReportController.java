package com.conectaedu.api.modules.report.controller;

import com.conectaedu.api.modules.report.dto.response.ClassProgressPointDTO;
import com.conectaedu.api.modules.report.dto.response.StudentReportResponseDTO;
import com.conectaedu.api.modules.report.interfaces.IReportFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//só GET e nenhum @Scheduled: o relatório é montado na hora em que alguém pergunta.
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportFacade reportFacade;

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'UNIVERSITY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<StudentReportResponseDTO> studentReport(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID schoolId) {
        return ResponseEntity.ok(reportFacade.getStudentReport(studentId, from, to, schoolId));
    }

    @GetMapping("/students/{studentId}/progress")
    @PreAuthorize("hasAnyRole('STUDENT', 'UNIVERSITY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<ClassProgressPointDTO>> classProgress(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID schoolId) {
        return ResponseEntity.ok(reportFacade.getClassProgress(studentId, from, to, schoolId));
    }
}
