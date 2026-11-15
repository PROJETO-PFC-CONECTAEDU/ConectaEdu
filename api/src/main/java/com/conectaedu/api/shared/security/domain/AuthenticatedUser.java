package com.conectaedu.api.shared.security.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class AuthenticatedUser extends User {
    private final UUID id;
    private final UUID universityId;
    private final UUID schoolId;

    public AuthenticatedUser(UUID id, String username, String password, UUID universityId, UUID schoolId, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.universityId = universityId;
        this.schoolId = schoolId;
    }
}
