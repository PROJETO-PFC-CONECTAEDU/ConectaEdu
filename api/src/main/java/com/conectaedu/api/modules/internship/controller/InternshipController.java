package com.conectaedu.api.modules.internship.controller;

import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.dto.response.UniversityInternshipResponseDTO;
import com.conectaedu.api.modules.internship.interfaces.IInternshipFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//Hora de estágio não se lança, se deriva da aula assinada.

@RestController
@RequestMapping("/api/v1/internship")
@RequiredArgsConstructor
public class InternshipController {

    private final IInternshipFacade internshipFacade;

    //Alimenta a tela de horas do estudante.

    @GetMapping("/students/{studentId}/hours")
    @PreAuthorize("hasAnyRole('STUDENT', 'UNIVERSITY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<InternshipHoursResponseDTO> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(internshipFacade.getHoursByStudent(studentId));
    }

    //Aqui que a faculdade acompanha, que é o critério da entrega.

    @GetMapping("/universities/{universityId}/hours")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<UniversityInternshipResponseDTO> getByUniversity(
            @PathVariable UUID universityId) {
        return ResponseEntity.ok(internshipFacade.getHoursByUniversity(universityId));
    }
}

