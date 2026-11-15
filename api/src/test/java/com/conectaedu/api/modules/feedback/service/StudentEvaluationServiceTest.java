package com.conectaedu.api.modules.feedback.service;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.demand.domain.DemandStudent;
import com.conectaedu.api.modules.demand.repository.DemandRepository;
import com.conectaedu.api.modules.feedback.domain.StudentEvaluation;
import com.conectaedu.api.modules.feedback.dto.request.StudentEvaluationRequestDTO;
import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.mapper.FeedbackMapper;
import com.conectaedu.api.modules.feedback.repository.StudentEvaluationRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
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
class StudentEvaluationServiceTest {

    private static final String TITULO = "Reforço de Matemática";
    private static final String NOME_ESTUDANTE = "Estudante Teste";

    @Mock
    private DemandRepository demandRepository;

    @Mock
    private StudentEvaluationRepository studentEvaluationRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private TenancyValidator tenancyValidator;

    //mapper real para a media calculada tambem entrar na verificacao.
    @Spy
    private FeedbackMapper feedbackMapper = new FeedbackMapper();

    @InjectMocks
    private StudentEvaluationService studentEvaluationService;

    private UUID diretorId;

    //o service le o diretor do contexto de seguranca, entao cada teste comeca com ele autenticado.
    @BeforeEach
    void autenticarDiretor() {
        diretorId = UUID.randomUUID();
        var permissoes = List.of(new SimpleGrantedAuthority("ROLE_SCHOOL_DIRECTOR"));
        AuthenticatedUser diretor = new AuthenticatedUser(
                diretorId, "diretor.teste@conectaedu.com", "", null, UUID.randomUUID(), permissoes);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(diretor, null, permissoes));
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
        necessidade.setFinishedAt(LocalDateTime.now().minusDays(1));
        return necessidade;
    }

    private StudentEvaluationRequestDTO request(UUID necessidadeId, UUID estudanteId) {
        return new StudentEvaluationRequestDTO(
                necessidadeId, estudanteId, 10, 8, 9, 7, 6, "Ótimo trabalho com a turma.");
    }

    //caminho feliz.
    @Test
    void deveRegistrarAvaliacaoDoEstudanteQuandoNecessidadeEstaConcluida() {
        UUID necessidadeId = UUID.randomUUID();
        UUID estudanteId = UUID.randomUUID();
        UUID idGerado = UUID.randomUUID();
        Demand necessidade = necessidade(necessidadeId, DemandStatus.FINISHED, estudante(estudanteId));
        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.of(necessidade));
        when(studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidadeId, estudanteId, diretorId)).thenReturn(false);
        when(studentEvaluationRepository.save(any(StudentEvaluation.class))).thenAnswer(chamada -> {
            StudentEvaluation salva = chamada.getArgument(0);
            salva.setId(idGerado);
            return salva;
        });
        ArgumentCaptor<StudentEvaluation> capturada = ArgumentCaptor.forClass(StudentEvaluation.class);

        StudentEvaluationResponseDTO resposta =
                studentEvaluationService.evaluate(request(necessidadeId, estudanteId));
        verify(tenancyValidator).validateDemandAccess(necessidade);
        verify(studentEvaluationRepository).save(capturada.capture());
        StudentEvaluation salva = capturada.getValue();
        assertEquals(necessidade, salva.getDemand());
        assertEquals(estudanteId, salva.getStudent().getId());
        assertEquals(diretorId, salva.getEvaluatedBy());
        assertEquals(10, salva.getKnowledge());
        assertEquals(8, salva.getPunctuality());
        assertEquals(9, salva.getTeaching());
        assertEquals(7, salva.getCommitment());
        assertEquals(6, salva.getEmpathy());
        assertEquals("Ótimo trabalho com a turma.", salva.getComments());
        assertNotNull(salva.getCreatedAt());

        assertEquals(idGerado, resposta.id());
        assertEquals(necessidadeId, resposta.demandId());
        assertEquals(diretorId, resposta.evaluatedBy());
        assertEquals(8.0, resposta.average());
        verify(auditService).logCreate(AuditEntityType.EVALUATION, idGerado,
                "Avaliação de %s em %s".formatted(NOME_ESTUDANTE, TITULO));
    }

    //a necessidade pode trocar de estudante no meio do ciclo.
    @Test
    void deveAvaliarEstudanteQueConstaApenasNoHistoricoDaNecessidade() {
        UUID necessidadeId = UUID.randomUUID();
        UUID estudanteAntigoId = UUID.randomUUID();
        Demand necessidade = necessidade(necessidadeId, DemandStatus.FINISHED, estudante(UUID.randomUUID()));
        DemandStudent vinculoAntigo = new DemandStudent();
        vinculoAntigo.setDemand(necessidade);
        vinculoAntigo.setStudent(estudante(estudanteAntigoId));
        vinculoAntigo.setActive(false);
        necessidade.getStudentHistory().add(vinculoAntigo);

        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.of(necessidade));
        when(studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidadeId, estudanteAntigoId, diretorId)).thenReturn(false);
        ArgumentCaptor<StudentEvaluation> capturada = ArgumentCaptor.forClass(StudentEvaluation.class);

        StudentEvaluationResponseDTO resposta =
                studentEvaluationService.evaluate(request(necessidadeId, estudanteAntigoId));

        verify(studentEvaluationRepository).save(capturada.capture());
        assertEquals(estudanteAntigoId, capturada.getValue().getStudent().getId());
        assertEquals(estudanteAntigoId, resposta.studentId());
    }

    @Test
    void deveLancarExcecaoQuandoNecessidadeNaoExiste() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(Optional.empty());

        DemandNotFoundException erro = assertThrows(DemandNotFoundException.class,
                () -> studentEvaluationService.evaluate(request(necessidadeId, UUID.randomUUID())));

        assertEquals("Necessidade não encontrada!", erro.getMessage());
        verify(studentEvaluationRepository, never()).save(any(StudentEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoNecessidadeAindaNaoFoiConcluida() {
        UUID necessidadeId = UUID.randomUUID();
        UUID estudanteId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.ONGOING, estudante(estudanteId))));

        InvalidDemandStatusException erro = assertThrows(InvalidDemandStatusException.class,
                () -> studentEvaluationService.evaluate(request(necessidadeId, estudanteId)));

        assertEquals("A Necessidade está com status ONGOING e só pode ser avaliada depois de concluída.",
                erro.getMessage());
        verify(studentEvaluationRepository, never()).save(any(StudentEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoEstudanteNaoAtendeuANecessidade() {
        UUID necessidadeId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.FINISHED, estudante(UUID.randomUUID()))));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> studentEvaluationService.evaluate(request(necessidadeId, UUID.randomUUID())));

        assertEquals("O estudante informado não atendeu esta Necessidade.", erro.getMessage());
        verify(studentEvaluationRepository, never()).save(any(StudentEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }

    @Test
    void deveLancarExcecaoQuandoDiretorJaAvaliouOEstudante() {
        UUID necessidadeId = UUID.randomUUID();
        UUID estudanteId = UUID.randomUUID();
        when(demandRepository.findById(necessidadeId)).thenReturn(
                Optional.of(necessidade(necessidadeId, DemandStatus.FINISHED, estudante(estudanteId))));
        when(studentEvaluationRepository.existsByDemand_IdAndStudent_IdAndEvaluatedBy(
                necessidadeId, estudanteId, diretorId)).thenReturn(true);

        EvaluationAlreadySubmittedException erro = assertThrows(EvaluationAlreadySubmittedException.class,
                () -> studentEvaluationService.evaluate(request(necessidadeId, estudanteId)));

        assertEquals("Este estudante já foi avaliado nesta Necessidade.", erro.getMessage());
        verify(studentEvaluationRepository, never()).save(any(StudentEvaluation.class));
        verify(auditService, never()).logCreate(any(), any(), any());
    }
}
