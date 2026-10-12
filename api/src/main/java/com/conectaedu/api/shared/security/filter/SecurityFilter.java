package com.conectaedu.api.shared.security.filter;

import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import com.conectaedu.api.shared.security.token.ValidateToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final ValidateToken validateToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var token = this.recoverToken(request);

        // Se não houver token, apenas segue para o próximo filtro (permitindo acesso a rotas públicas)
        if (token != null) {
            var decodedJWT = validateToken.validateToken(token);
            if (decodedJWT != null) {

                String email = decodedJWT.getSubject();

                String role = decodedJWT.getClaim("role").asString();

                String id = decodedJWT.getClaim("id").asString();

                if (id != null) {
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));

                    var userDetails = new AuthenticatedUser(UUID.fromString(id), email, "", authorities);

                    var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    public String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }

}
