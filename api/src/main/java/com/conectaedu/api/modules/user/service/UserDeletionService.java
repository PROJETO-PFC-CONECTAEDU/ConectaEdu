package com.conectaedu.api.modules.user.service;

import com.conectaedu.api.modules.user.domain.User;
import com.conectaedu.api.modules.user.repository.UserRepository;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDeletionService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado!"));

        userRepository.deleteById(id);

        auditService.logDelete(AuditEntityType.USER, user.getId(), user.getName());
    }
}
