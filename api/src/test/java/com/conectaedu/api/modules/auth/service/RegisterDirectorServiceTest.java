package com.conectaedu.api.modules.auth.service;

import com.conectaedu.api.modules.auth.dto.SchoolDirectorRegisterDTO;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import com.conectaedu.api.modules.user.genericUser.repository.UserRepository;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
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

//regras, um e-mail so pode ser cadastrado uma vez, o diretor nasce inativo, sem escola vinculada e com e-mail pendente de confirmacao.

@ExtendWith(MockitoExtension.class)
class RegisterDirectorServiceTest {

    private static final String NOME = "Maria Sousa";
    private static final String EMAIL = "Maria.Sousa@ConectaEdu.com";
    private static final String SENHA = "Senha@123";
    private static final String SENHA_CODIFICADA = "hash-da-senha";
    private static final String CIE = "123456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SendEmailConfirmationService emailConfirmationService;

    @InjectMocks
    private RegisterDirectorService registerDirectorService;

    private SchoolDirectorRegisterDTO request() {
        return new SchoolDirectorRegisterDTO(NOME, EMAIL, SENHA, "  " + CIE + " ");
    }

    //caminho feliz.
    @Test
    void deveCadastrarDiretorEEnviarCodigoDeConfirmacao() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        ArgumentCaptor<SchoolDirector> capturado = ArgumentCaptor.forClass(SchoolDirector.class);

        registerDirectorService.register(request());

        verify(userRepository).save(capturado.capture());
        SchoolDirector salvo = capturado.getValue();
        assertTrue(salvo.getEmailConfirmationToken().matches("\\d{6}"));
        verify(emailConfirmationService).sendConfirmationEmail(
                EMAIL.toLowerCase(), salvo.getEmailConfirmationToken());
    }

    //primeiro estagio do diretor recem-cadastrado, inativo, sem escola, e-mail nao confirmado, senha codificada, e-mail em minusculas e CIE sem espacos.
    @Test
    void deveSalvarDiretorNovoInativoESemEscolaVinculada() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA)).thenReturn(SENHA_CODIFICADA);
        ArgumentCaptor<SchoolDirector> capturado = ArgumentCaptor.forClass(SchoolDirector.class);

        registerDirectorService.register(request());

        verify(userRepository).save(capturado.capture());
        SchoolDirector salvo = capturado.getValue();
        assertEquals(NOME, salvo.getName());
        assertEquals("maria.sousa@conectaedu.com", salvo.getEmail());
        assertEquals(SENHA_CODIFICADA, salvo.getPassword());
        assertEquals(CIE, salvo.getSchoolCIE());
        assertEquals(UserType.PERSON, salvo.getUserType());
        assertEquals(UserRole.SCHOOL_DIRECTOR, salvo.getUserRole());
        assertNull(salvo.getSchool());
        assertFalse(salvo.isActive());
        assertFalse(salvo.isEmailConfirmed());
        assertNotNull(salvo.getCreatedAt());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstaCadastrado() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        EmailAlreadyExistsException erro = assertThrows(EmailAlreadyExistsException.class,
                () -> registerDirectorService.register(request()));

        assertEquals("E-mail já cadastrado", erro.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailConfirmationService, never()).sendConfirmationEmail(anyString(), anyString());
    }
}
