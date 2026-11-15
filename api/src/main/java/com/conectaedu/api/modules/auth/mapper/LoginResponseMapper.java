package com.conectaedu.api.modules.auth.mapper;

import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoginResponseMapper {

    public LoginResponseDTO toResponse(User userEntity, String token) {
        UUID universityId = null;
        UUID schoolId = null;

        if (userEntity instanceof Student student && student.getUniversity() != null) {
            universityId = student.getUniversity().getId();
        } else if (userEntity instanceof UniversityAdmin admin && admin.getUniversity() != null) {
            universityId = admin.getUniversity().getId();
        } else if (userEntity instanceof SchoolDirector director && director.getSchool() != null) {
            schoolId = director.getSchool().getId();
        }

        return new LoginResponseDTO(
                token,
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getUserRole().name(),
                universityId,
                schoolId,
                userEntity.is2faEnabled()
        );
    }
}
