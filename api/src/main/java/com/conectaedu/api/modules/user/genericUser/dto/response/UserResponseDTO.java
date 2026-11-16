package com.conectaedu.api.modules.user.genericUser.dto.response;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        UserType userType,
        UserRole userRole,
        boolean active,
        boolean emailConfirmed,
        String institutionName,
        String universityCNPJ,
        String schoolCIE
) {
    //  Mapper "manual"
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                user.getUserRole(),
                user.isActive(),
                user.isEmailConfirmed(),
                getInstitutionName(user),
                getUniversityCNPJ(user),
                getSchoolCIE(user)
        );
    }

    private static String getUniversityCNPJ(User user) {
        if (user instanceof com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin universityAdmin) {
            return universityAdmin.getUniversityCNPJ();
        }
        return null;
    }

    private static String getSchoolCIE(User user) {
        if (user instanceof com.conectaedu.api.modules.user.school_director.domain.SchoolDirector schoolDirector) {
            return schoolDirector.getSchoolCIE();
        }
        return null;
    }

    private static String getInstitutionName(User user) {
        if (user instanceof com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin universityAdmin) {
            return universityAdmin.getUniversity() != null ? universityAdmin.getUniversity().getName() : null;
        }
        if (user instanceof com.conectaedu.api.modules.user.school_director.domain.SchoolDirector schoolDirector) {
            return schoolDirector.getSchool() != null ? schoolDirector.getSchool().getName() : null;
        }
        if (user instanceof com.conectaedu.api.modules.user.student.domain.Student student) {
            return student.getUniversity() != null ? student.getUniversity().getName() : null;
        }
        return null;
    }
}
