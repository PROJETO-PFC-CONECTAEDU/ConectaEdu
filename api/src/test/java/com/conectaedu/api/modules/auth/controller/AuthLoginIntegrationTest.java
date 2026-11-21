package com.conectaedu.api.modules.auth.controller;

import com.conectaedu.api.modules.auth.dto.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveBloquearIPAposMuitasTentativasFalhas() throws Exception {
        String email = "test@example.com";
        String password = "wrong-password";
        LoginRequestDTO request = new LoginRequestDTO(email, password);
        String content = objectMapper.writeValueAsString(request);
        String remoteAddr = "1.2.3.4";

        // Realiza 5 tentativas falhas
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .with(req -> {
                                req.setRemoteAddr(remoteAddr);
                                return req;
                            }))
                    .andExpect(status().isUnauthorized());
        }

        // A 6ª tentativa deve ser bloqueada com 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .with(req -> {
                            req.setRemoteAddr(remoteAddr);
                            return req;
                        }))
                .andExpect(status().isTooManyRequests());
    }
}
