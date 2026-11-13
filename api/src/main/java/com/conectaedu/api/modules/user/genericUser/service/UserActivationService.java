package com.conectaedu.api.modules.user.genericUser.service;

import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@PreAuthorize( "hasRole('PLATFORM_ADMIN')")
public class UserActivationService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDTO activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return new UserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return new UserResponseDTO(user);
    }
}
