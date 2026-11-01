package com.conectaedu.api.modules.internship.dto.response;

import java.util.List;
import java.util.UUID;

//O requiredHours do topo é a regra da universidade, cada estudante repete a carga que vale para ele.

public record UniversityInternshipResponseDTO(
        UUID universityId,
        int requiredHours,
        List<InternshipHoursResponseDTO> students) {}
