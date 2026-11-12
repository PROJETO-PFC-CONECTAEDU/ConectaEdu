package com.conectaedu.api.shared.security.filter;

import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetailsService {

    private final UserRepository userRepository;

    @Override //LoadByUsername para ser acessivel pelo spring security
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));

        UUID universityId = null;
        UUID schoolId = null;
        if (user instanceof Student student) {
            if (student.getUniversity() != null) {
                universityId = student.getUniversity().getId();
            }
        } else if (user instanceof UniversityAdmin admin) {
            if (admin.getUniversity() != null) {
                universityId = admin.getUniversity().getId();
            }
        } else if (user instanceof SchoolDirector director) {
            if (director.getSchool() != null) {
                schoolId = director.getSchool().getId();
            }
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                universityId,
                schoolId,
                authorities);
    }
}
