package com.conectaedu.api.shared.security.context;

import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public class SecurityContextHolder {

    public static AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        return null;
    }

    public static UUID getAuthenticatedUserId() {
        AuthenticatedUser user = getAuthenticatedUser();
        return user != null ? user.getId() : null;
    }

    public static String getAuthenticatedUserEmail() {
        AuthenticatedUser user = getAuthenticatedUser();
        return user != null ? user.getUsername() : null;
    }
}
