package com.conectaedu.api.modules.feedback.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.domain.DemandStudent;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.ClassEvaluation;
import com.conectaedu.api.modules.feedback.dto.request.ClassEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.ClassEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.mapper.FeedbackMapper;
import com.conectaedu.api.modules.feedback.repository.ClassEvaluationRepository;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.audit.service.AuditService;
import com.conectaedu.api.shared.enums.AuditEntityType;
import com.conectaedu.api.shared.enums.DemandStatus;
import com.conectaedu.api.shared.exceptions.DemandNotFoundException;
import com.conectaedu.api.shared.exceptions.EvaluationAlreadySubmittedException;
import com.conectaedu.api.shared.exceptions.InvalidDemandStatusException;
import com.conectaedu.api.shared.security.domain.AuthenticatedUser;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassEvaluationServiceTest {

    private static final String TITULO = "Reforço de Matemática";
    private static final String NOME_ESTUDANTE = "Estudante Teste";

    @Mock
    private DemandRepository demandRepository;

    @Mock
    private ClassEvaluationRepository classEvaluationRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private TenancyValidator tenancyValidator;

    //mapper real, para a media calculada tambem entrar na verificacao.
    @Spy
    private FeedbackMapper feedbackMapper = new FeedbackMapper();

    @InjectMocks
    private ClassEvaluationService classEvaluationService;

    private UUID estudanteId;

    //o service le o estudante do contexto de seguranca, entao cada teste comeca com ele autenticado.
    @BeforeEach
    void autenticarEstudante() {
        estudanteId = UUID.randomUUID();
        var permissoes = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        AuthenticatedUser estudante = new AuthenticatedUser(
                estudanteId, "estudante.teste@conectaedu.com", "", UUID.randomUUID(), null, permissoes);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(estudante, null, permissoes));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private Student estudante(UUID id) {
        Student estudante = new Student();
        estudante.setId(id);
        estudante.setName(NOME_ESTUDANTE);
        return estudante;
    }

    private Demand necessidade(UUID id, DemandStatus status, Student estudante) {
        School escola = new School();
        escola.setId(UUID.randomUUID());

        Demand necessidade = new Demand();
        necessidade.setId(id);
        necessidade.setTitle(TITULO);
        necessidade.setStatus(status);
        necessidade.setSchool(escola);
        necessidade.setStudent(estudante);
        return necessidade;
    }

    private ClassEvaluationRequestDTO request(UUID necessidadeId) {
        return new ClassEvaluationRequestDTO(necessidadeId, 8, 6, 10, "Turma participativa.");
    }

    //caminho feliz no qual o estudante que atendeu a necessidade concluida avalia a sala.
    @Test
    void deveRegistrarAvaliacaoDaSalaQuandoNecessidadeEstaConcluida() {
        UUID necessidadeId = UUID.randomUUID();
        UUID idGerado = UUID.randomUUID();
        Demand necessidade = necessidade(necessidadeId, DemandStatus.FINISHED, estudante(estudanteId));
        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.of(necessidade));
        when(classEvaluationRepository.existsByDemand_IdAndStudent_Id(necessidadeId, estudanteId))
                .thenReturn(false);
        when(classEvaluationRepository.save(any(ClassEvaluation.class))).thenAnswer(chamada -> {
            ClassEvaluation salva = chamada.getArgument(0);
            salva.setId(idGerado);
            return salva;
        });
        ArgumentCaptor<ClassEvaluation> capturada = ArgumentCaptor.forClass(ClassEvaluation.class);

        ClassEvaluationResponseDTO resposta = classEvaluationService.evaluate(request(necessidadeId));

        verify(tenancyValidator).validateDemandAccess(necessidade);
        verify(classEvaluationRepository).save(capturada.capture());
        ClassEvaluation salva = capturada.getValue();
        assertEquals(necessidade, salva.getDemand());
        assertEquals(estudanteId, salva.getStudent().getId());
        assertEquals(8, salva.getEngagement());
        assertEquals(6, salva.getProgress());
        assertEquals(10, salva.getInfrastructure());
        assertEquals("Turma participativa.", salva.getComments());
        assertNotNull(salva.getCreatedAt());

        assertEquals(idGerado, resposta.id());
        assertEquals(necessidadeId, resposta.demandId());
        assertEquals(estudanteId, resposta.studentId());
        assertEquals(NOME_ESTUDANTE, resposta.studentName());
        verify(auditService).logCreate(AuditEntityType.EVALUATION, idGerado,
                "Avaliação da sala de %s por %s".formatted(TITULO, NOME_ESTUDANTE));
    }

    @Test
    void devePermitirAvaliacaoDeEstudanteQueConstaApenasNoHistorico() {
        UUID necessidadeId = UUID.randomUUID();
        Demand necessidade = necessidade(necessidadeId, DemandStatus.FINISHED, estudante(UUID.randomUUID()));
        DemandStudent vinculoAntigo = new DemandStudent();
        vinculoAntigo.setDemand(necessidade);
        vinculoAntigo.setStudent(estudante(estudanteId));
        vinculoAntigo.setActive(false);
        necessidade.getStudentHistory().add(vinculoAntigo);

        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.of(necessidade));
        when(classEvaluationRepository.existsByDemand_IdAndStudent_Id(necessidadeId, estudanteId))
                .thenReturn(false);
        ArgumentCaptor<ClassEvaluation> capturada = ArgumentCaptor.forClass(ClassEvaluation.class);

        ClassEvaluationResponseDTO resposta = classEvaluationService.evaluate(request(necessidadeId));

        verify(classEvaluationRepository).save(capturada.capture());
        assertEquals(estudanteId, capturada.getValue().getStudent().getId());
        assertEquals(estudanteId, resposta.studentId());
    }

    @Test
    void deveLancarExcecaoQuandoNecessidadeNaoExiste() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.empty());

        DemandNotFoundException erro = assertThrows(DemandNotFoundException.class,
                () -> classEvaluationService.evaluate(request(necessidadeId)));

        assertEquals("Necessidade não encontrada!", erro.getMessage());
        verify(classEvaluationRepository, never()).save(any(ClassEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoNecessidadeAindaNaoFoiConcluida() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.ONGOING, estudante(estudanteId))));

        InvalidDemandStatusException erro = assertThrows(InvalidDemandStatusException.class,
                () -> classEvaluationService.evaluate(request(necessidadeId)));

        assertEquals("A Necessidade está com status ONGOING e só pode ser avaliada depois de concluída.",
                erro.getMessage());
        verify(classEvaluationRepository, never()).save(any(ClassEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoEstudanteAutenticadoNaoAtendeuANecessidade() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.FINISHED, estudante(UUID.randomUUID()))));

        AccessDeniedException erro = assertThrows(AccessDeniedException.class,
                () -> classEvaluationService.evaluate(request(necessidadeId)));

        assertEquals("Apenas o estudante que atendeu a necessidade pode avaliar a sala.", erro.getMessage());
        verify(classEvaluationRepository, never()).save(any(ClassEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoEstudanteJaAvaliouASala() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.FINISHED, estudante(estudanteId))));
        when(classEvaluationRepository.existsByDemand_IdAndStudent_Id(necessidadeId, estudanteId))
                .thenReturn(true);

        EvaluationAlreadySubmittedException erro = assertThrows(EvaluationAlreadySubmittedException.class,
                () -> classEvaluationService.evaluate(request(necessidadeId)));

        assertEquals("Você já avaliou a sala desta Necessidade.", erro.getMessage());
        verify(classEvaluationRepository, never()).save(any(ClassEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }
}
