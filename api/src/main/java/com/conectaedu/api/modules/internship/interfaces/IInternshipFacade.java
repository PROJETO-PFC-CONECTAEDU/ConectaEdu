package com.conectaedu.api.modules.internship.interfaces;

import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.dto.response.UniversityInternshipResponseDTO;

import java.util.UUID;

public interface IInternshipFacade {

    InternshipHoursResponseDTO getHoursByStudent(UUID studentId);

    UniversityInternshipResponseDTO getHoursByUniversity(UUID universityId);
}
