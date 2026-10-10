package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.LoginRequestDTO;
import com.conectaedu.api.modules.auth.dto.LoginResponseDTO;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.security.token.GenerateToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthLoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final GenerateToken generateToken;

    public LoginResponseDTO login(LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        var user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();

        User userEntity = userRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String token = generateToken.generateToken(userEntity);

        return new LoginResponseDTO(
                token,
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getUserRole().name()
        );
    }
}
