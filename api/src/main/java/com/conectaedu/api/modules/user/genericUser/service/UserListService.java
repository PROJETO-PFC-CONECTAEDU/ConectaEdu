package com.conectaedu.api.modules.user.genericUser.service;

import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserListService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> listAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }
}
