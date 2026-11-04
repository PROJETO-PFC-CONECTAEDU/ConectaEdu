package com.conectaedu.api.modules.report.facade;

import com.conectaedu.api.modules.report.dto.response.ClassProgressPointDTO;
import com.conectaedu.api.modules.report.dto.response.StudentReportResponseDTO;
import com.conectaedu.api.modules.report.interfaces.IReportFacade;
import com.conectaedu.api.modules.report.service.StudentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportFacadeImpl implements IReportFacade {

    private final StudentReportService studentReportService;

    @Override
    public StudentReportResponseDTO getStudentReport(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        return studentReportService.forStudent(studentId, from, to, schoolId);
    }

    @Override
    public List<ClassProgressPointDTO> getClassProgress(UUID studentId, LocalDate from, LocalDate to, UUID schoolId) {
        return studentReportService.classProgress(studentId, from, to, schoolId);
    }
}
