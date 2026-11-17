package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.UniversityAdminRegisterDTO;
import com.conectaedu.api.modules.university.repository.UniversityRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.university_admin.domain.UniversityAdmin;
import com.conectaedu.api.shared.email.service.SendEmailConfirmationService;
import com.conectaedu.api.shared.enums.UserRole;
import com.conectaedu.api.shared.enums.UserType;
import com.conectaedu.api.shared.exceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//regras, um e-mail so pode ser cadastrado uma vez, o administrador nasce inativo, sem universidade vinculada e com e-mail pendente de confirmacao.

@ExtendWith(MockitoExtension.class)
class RegisterUniversityAdminServiceTest {

    private static final String NOME = "Carlos Almeida";
    private static final String EMAIL = "Carlos.Almeida@ConectaEdu.com";
    private static final String SENHA = "Senha@123";
    private static final String SENHA_CODIFICADA = "hash-da-senha";
    private static final String CNPJ = "12345678000195";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SendEmailConfirmationService emailConfirmationService;

    @InjectMocks
    private RegisterUniversityAdminService registerUniversityAdminService;

    private UniversityAdminRegisterDTO request() {
        return new UniversityAdminRegisterDTO(NOME, EMAIL, SENHA, "  " + CNPJ + " ");
    }

    //caminho feliz, e-mail livre, administrador salvo e codigo de confirmacao enviado para o e-mail dele.
    @Test
    void deveCadastrarAdministradorEEnviarCodigoDeConfirmacao() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        ArgumentCaptor<UniversityAdmin> capturado = ArgumentCaptor.forClass(UniversityAdmin.class);

        registerUniversityAdminService.register(request());

        verify(userRepository).save(capturado.capture());
        UniversityAdmin salvo = capturado.getValue();
        assertTrue(salvo.getEmailConfirmationToken().matches("\\d{6}"));
        verify(emailConfirmationService).sendConfirmationEmail(
                EMAIL.toLowerCase(), salvo.getEmailConfirmationToken());
    }

    //primeiro estagio do administrador recem-cadastrado, inativo, sem universidade, e-mail nao confirmado,
    //senha codificada, e-mail em minusculas e CNPJ sem espacos nas pontas.
    @Test
    void deveSalvarAdministradorNovoInativoESemUniversidadeVinculada() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        ArgumentCaptor<UniversityAdmin> capturado = ArgumentCaptor.forClass(UniversityAdmin.class);

        registerUniversityAdminService.register(request());

        verify(userRepository).save(capturado.capture());
        UniversityAdmin salvo = capturado.getValue();
        assertEquals(NOME, salvo.getName());
        assertEquals("carlos.almeida@conectaedu.com", salvo.getEmail());
        assertEquals(SENHA_CODIFICADA, salvo.getPassword());
        assertEquals(CNPJ, salvo.getUniversityCNPJ());
        assertEquals(UserType.PERSON, salvo.getUserType());
        assertEquals(UserRole.UNIVERSITY_ADMIN, salvo.getUserRole());
        assertNull(salvo.getUniversity());
        assertFalse(salvo.isActive());
        assertFalse(salvo.isEmailConfirmed());
        assertNotNull(salvo.getCreatedAt());
    }

    //e-mail repetido bloqueia o cadastro e nenhum e-mail e enviado.
    @Test
    void deveLancarExcecaoQuandoEmailJaEstaCadastrado() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        EmailAlreadyExistsException erro = assertThrows(EmailAlreadyExistsException.class,
                () -> registerUniversityAdminService.register(request()));

        assertEquals("E-mail já cadastrado", erro.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailConfirmationService, never()).sendConfirmationEmail(anyString(), anyString());
    }
}
