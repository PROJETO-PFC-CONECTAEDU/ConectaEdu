package com.conectaedu.api.modules.user.genericUser.facade;

import com.conectaedu.api.modules.user.genericUser.facade.dto.request.UserCreationRequestDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.request.UserUpdateRequestDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserCreationResponseDTO;
import com.conectaedu.api.modules.user.genericUser.facade.dto.response.UserResponseDTO;
import com.conectaedu.api.modules.user.genericUser.interfaces.IUserFacade;
import com.conectaedu.api.modules.user.genericUser.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements IUserFacade {

    private final UserCreationService userCreationService;
    private final UserUpdateService userUpdateService;
    private final UserDeletionService userDeletionService;
    private final UserListService userListService;
    private final UserListByService userListByService;

    @Override
    public UserCreationResponseDTO createUser(UserCreationRequestDTO request) {
        return userCreationService.createUser(request);
    }

    @Override
    public UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO request) {
        return userUpdateService.updateUser(id, request);
    }

    @Override
    public void deleteUser(UUID id) {
        userDeletionService.deleteUser(id);
    }

    @Override
    public UserResponseDTO getUserById(UUID id) {
        return userListByService.listById(id);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        return userListByService.listByEmail(email);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userListService.listAll();
    }
}
