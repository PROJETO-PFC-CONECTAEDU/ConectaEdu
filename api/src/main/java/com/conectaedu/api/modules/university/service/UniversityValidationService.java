package com.conectaedu.api.modules.university.service;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.dto.response.UniversityResponseDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.shared.enums.UniversityStatus;
import com.conectaedu.api.shared.exceptions.InvalidUniversityStatusException;
import com.conectaedu.api.shared.exceptions.UniversityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityValidationService {

    private final UniversityRepository universityRepository;

    //Aprova a universidade: passa a APPROVED e é ativada.
    @Transactional
    public UniversityResponseDTO validateUniversity(UUID id, String notes) {
        University university = findPending(id);

        university.setStatus(UniversityStatus.APPROVED);
        university.setActive(true);
        university.setValidatedAt(LocalDateTime.now());
        university.setValidationNotes(notes);
        university.setUpdatedAt(LocalDateTime.now());

        universityRepository.save(university);
        return new UniversityResponseDTO(university);
    }

    //Recusa a universidade: passa a REJECTED e continua inativa.
    @Transactional
    public UniversityResponseDTO rejectUniversity(UUID id, String notes) {
        //A universidade precisa saber o motivo da recusa.
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException("Recusa exige um parecer justificado");
        }

        University university = findPending(id);

        university.setStatus(UniversityStatus.REJECTED);
        university.setActive(false);
        university.setValidatedAt(LocalDateTime.now());
        university.setValidationNotes(notes);
        university.setUpdatedAt(LocalDateTime.now());

        universityRepository.save(university);
        return new UniversityResponseDTO(university);
    }

    //Só universidade pendente entra em validação. APPROVED e REJECTED são finais.
    private University findPending(UUID id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new UniversityNotFoundException("Universidade não encontrada!"));

        if (university.getStatus() != UniversityStatus.PENDING) {
            throw new InvalidUniversityStatusException(
                    "Universidade já avaliada. Situação atual: " + university.getStatus());
        }
        return university;
    }
}
