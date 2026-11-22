package com.conectaedu.api.modules.user.genericUser.interfaces;

import com.conectaedu.api.modules.user.genericUser.dto.request.UserCreationRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.request.UserInstitutionBindingRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.request.UserUpdateRequestDTO;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserCreationResponseDTO;
import com.conectaedu.api.modules.user.genericUser.dto.response.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IUserFacade {
    UserCreationResponseDTO createUser(UserCreationRequestDTO request);
    UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO request);
    void deleteUser(UUID id);
    UserResponseDTO getUserById(UUID id);
    UserResponseDTO getUserByEmail(String email);
    List<UserResponseDTO> getAllUsers(String search);
    UserResponseDTO activateUser(UUID id);
    UserResponseDTO deactivateUser(UUID id);
    UserResponseDTO bindInstitution(UUID userId, UserInstitutionBindingRequestDTO request);
}
