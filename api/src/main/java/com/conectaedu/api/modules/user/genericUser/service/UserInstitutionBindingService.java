package com.conectaedu.api.modules.user.genericUser.service;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.dto.request.UserInstitutionBindingRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.audit.support.AuditDiff;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class UserInstitutionBindingService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    @Transactional
    public UserResponseDTO bindInstitution(UUID userId, UserInstitutionBindingRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        AuditDiff diff = AuditDiff.create();

        if (user instanceof Student student) {
            University university = universityRepository.findById(request.institutionId())
                    .orElseThrow(() -> new IllegalArgumentException("Universidade não encontrada!"));
            
            String beforeUniversity = student.getUniversity() != null ? student.getUniversity().getName() : "Nenhuma";
            student.setUniversity(university);
            diff.field("university", beforeUniversity, university.getName());
            
        } else if (user instanceof UniversityAdmin universityAdmin) {
            University university = universityRepository.findById(request.institutionId())
                    .orElseThrow(() -> new IllegalArgumentException("Universidade não encontrada!"));
            
            String beforeUniversity = universityAdmin.getUniversity() != null ? universityAdmin.getUniversity().getName() : "Nenhuma";
            universityAdmin.setUniversity(university);
            universityAdmin.setUniversityCNPJ(university.getCnpj());
            diff.field("university", beforeUniversity, university.getName());
            diff.field("universityCNPJ", null, university.getCnpj());

        } else if (user instanceof SchoolDirector schoolDirector) {
            School school = schoolRepository.findById(request.institutionId())
                    .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada!"));
            
            String beforeSchool = schoolDirector.getSchool() != null ? schoolDirector.getSchool().getName() : "Nenhuma";
            schoolDirector.setSchool(school);
            schoolDirector.setSchoolCIE(school.getCie());
            diff.field("school", beforeSchool, school.getName());
            diff.field("schoolCIE", null, school.getCie());

        } else {
            throw new IllegalArgumentException("Tipo de usuário não suporta vínculo com instituição!");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.logUpdate(AuditEntityType.USER, user.getId(), user.getName(), diff);

        return new UserResponseDTO(user);
    }
}
