package com.conectaedu.api.modules.consent.service;

import com.conectaedu.api.modules.consent.dto.response.ConsentRecordResponseDTO;
import com.conectaedu.api.modules.consent.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentListService {

    private final ConsentRecordRepository consentRecordRepository;

    //Somente leitura
    @Transactional(readOnly = true)
    public List<ConsentRecordResponseDTO> listByUser(UUID userId) {
        return consentRecordRepository.findByUser_IdOrderByAcceptedAtDesc(userId)
                .stream()
                .map(ConsentRecordResponseDTO::new)
                .toList();
    }
}
