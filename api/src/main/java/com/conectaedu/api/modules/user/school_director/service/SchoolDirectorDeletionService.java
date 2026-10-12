package com.conectaedu.api.modules.user.school_director.service;

import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.school_director.repository.SchoolDirectorRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolDirectorDeletionService {

    private final SchoolDirectorRepository schoolDirectorRepository;
    private final AuditService auditService;

    @Transactional
    public void deleteSchoolDirector(UUID id) {
        SchoolDirector director = schoolDirectorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Diretor de escola não encontrado!"));

        schoolDirectorRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.SCHOOL_DIRECTOR, director.getId(), director.getName());
    }
}
