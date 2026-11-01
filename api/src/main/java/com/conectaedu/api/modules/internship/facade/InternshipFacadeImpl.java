package com.conectaedu.api.modules.internship.facade;

import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.dto.response.UniversityInternshipResponseDTO;
import com.conectaedu.api.modules.internship.interfaces.IInternshipFacade;
import com.conectaedu.api.modules.internship.service.InternshipHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InternshipFacadeImpl implements IInternshipFacade {

    private final InternshipHoursService internshipHoursService;

    @Override
    public InternshipHoursResponseDTO getHoursByStudent(UUID studentId) {
        return internshipHoursService.forStudent(studentId);
    }

    @Override
    public UniversityInternshipResponseDTO getHoursByUniversity(UUID universityId) {
        return internshipHoursService.forUniversity(universityId);
    }
}
