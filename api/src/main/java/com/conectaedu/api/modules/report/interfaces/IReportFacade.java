package com.conectaedu.api.modules.report.interfaces;

import com.conectaedu.api.modules.report.dto.response.ClassProgressPointDTO;
import com.conectaedu.api.modules.report.dto.response.StudentReportResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IReportFacade {

    StudentReportResponseDTO getStudentReport(UUID studentId, LocalDate from, LocalDate to, UUID schoolId);

    List<ClassProgressPointDTO> getClassProgress(UUID studentId, LocalDate from, LocalDate to, UUID schoolId);
}
