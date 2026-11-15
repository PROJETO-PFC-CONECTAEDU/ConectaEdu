package com.conectaedu.api.modules.report.service;

import com.conectaedu.api.modules.feedback.dto.response.StudentEvaluationResponseDTO;
import com.conectaedu.api.modules.feedback.interfaces.IFeedbackFacade;
import com.conectaedu.api.modules.internship.dto.response.InternshipHoursResponseDTO;
import com.conectaedu.api.modules.internship.interfaces.IInternshipFacade;
import com.conectaedu.api.modules.lesson.domain.Lesson;
import com.conectaedu.api.modules.report.dto.response.ClassProgressPointDTO;
import com.conectaedu.api.modules.report.dto.response.StudentReportResponseDTO;
import com.conectaedu.api.modules.report.repository.ReportLessonRepository;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.school.repository.SchoolRepository;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.modules.user.student.repository.StudentRepository;
import com.conectaedu.api.modules.user.university_admin.repository.UniversityAdminRepository;
import com.conectaedu.api.shared.enums.LessonStatus;
import com.conectaedu.api.shared.exceptions.SchoolNotFoundException;
import com.conectaedu.api.shared.exceptions.UserNotFoundException;
import com.conectaedu.api.shared.security.util.TenancyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//Regras, o periodo e a escola filtram as atividades e as avaliacoes, nunca a carga do estagio.
//Hora validada vem de aula VALIDATED, hora pendente de aula COMPLETED, e a serie do Nine Box
//so recebe aula realizada que tenha os dois eixos preenchidos.

@ExtendWith(MockitoExtension.class)
class StudentReportServiceTest {

    private static final String NOME = "Maria S";
    private static final LocalDate INICIO = LocalDate.of(2026, 3, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 3, 31);

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UniversityAdminRepository universityAdminRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private ReportLessonRepository reportLessonRepository;

    @Mock
    private IInternshipFacade internshipFacade;

    @Mock
    private IFeedbackFacade feedbackFacade;

    @Mock
    private TenancyValidator tenancyValidator;

    @Captor
    private ArgumentCaptor<Collection<LessonStatus>> situacoesConsultadas;

    @InjectMocks
    private StudentReportService studentReportService;

    private UUID estudanteId;
    private Student estudante;
    private School escola;
    private School outraEscola;

    @BeforeEach
    void prepararCenario() {
        estudanteId = UUID.randomUUID();
        estudante = new Student();
        estudante.setId(estudanteId);
        estudante.setName(NOME);

        escola = escola();
        outraEscola = escola();
    }

    private School escola() {
        School escola = new School();
        escola.setId(UUID.randomUUID());
        return escola;
    }

    private Lesson aula(LessonStatus situacao, LocalDateTime inicio, int minutos, School escola) {
        Lesson aula = new Lesson();
        aula.setId(UUID.randomUUID());
        aula.setStatus(situacao);
        aula.setScheduledStart(inicio);
        aula.setScheduledEnd(inicio.plusMinutes(minutos));
        aula.setSchool(escola);
        return aula;
    }

    private Lesson comNineBox(Lesson aula, int desempenho, int potencial) {
        aula.setNineBoxPerformance(desempenho);
        aula.setNineBoxPotential(potencial);
        return aula;
    }

    private LocalDateTime dia(int dia) {
        return LocalDateTime.of(2026, 3, dia, 8, 0);
    }

    private InternshipHoursResponseDTO horas() {
        return new InternshipHoursResponseDTO(
                estudanteId, NOME, new BigDecimal("12.5"), 5, 1, 1,
                400, new BigDecimal("387.5"), null);
    }

    private StudentEvaluationResponseDTO avaliacao(UUID escolaId, double media, LocalDateTime finalizadaEm) {
        return new StudentEvaluationResponseDTO(
                UUID.randomUUID(), UUID.randomUUID(), "Reforço de matemática", escolaId,
                estudanteId, NOME, UUID.randomUUID(),
                4, 4, 4, 4, 4, media, "Bom trabalho", finalizadaEm, finalizadaEm);
    }

    private void estudanteEncontrado() {
        when(studentRepository.findById(estudanteId)).thenReturn(Optional.of(estudante));
    }

    private void aulasEncontradas(List<Lesson> aulas) {
        when(reportLessonRepository.findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                eq(estudanteId), any(), any(), any())).thenReturn(aulas);
    }

    private void relatorioCom(List<Lesson> aulas, List<StudentEvaluationResponseDTO> avaliacoes) {
        estudanteEncontrado();
        aulasEncontradas(aulas);
        when(internshipFacade.getHoursByStudent(estudanteId)).thenReturn(horas());
        when(feedbackFacade.getEvaluationsByStudent(estudanteId, null, null)).thenReturn(avaliacoes);
    }

    //caminho feliz, cada situacao de aula cai no indicador certo e a carga vem do modulo de estagio.
    @Test
    void deveMontarRelatorioComOsIndicadoresDoPeriodo() {
        Lesson validadaLonga = comNineBox(aula(LessonStatus.VALIDATED, dia(2), 90, escola), 2, 3);
        Lesson validadaCurta = aula(LessonStatus.VALIDATED, dia(5), 60, escola);
        Lesson pendente = aula(LessonStatus.COMPLETED, dia(9), 30, escola);
        Lesson cancelada = aula(LessonStatus.CANCELED, dia(12), 60, escola);
        Lesson naoRealizada = aula(LessonStatus.NOT_COMPLETED, dia(15), 60, escola);
        StudentEvaluationResponseDTO avaliacao = avaliacao(escola.getId(), 4.0, dia(20));

        estudanteEncontrado();
        when(reportLessonRepository.findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                eq(estudanteId), any(), eq(INICIO.atStartOfDay()), eq(FIM.atTime(LocalTime.MAX))))
                .thenReturn(List.of(validadaLonga, validadaCurta, pendente, cancelada, naoRealizada));
        when(internshipFacade.getHoursByStudent(estudanteId)).thenReturn(horas());
        when(feedbackFacade.getEvaluationsByStudent(estudanteId, null, null)).thenReturn(List.of(avaliacao));

        StudentReportResponseDTO relatorio = studentReportService.forStudent(estudanteId, INICIO, FIM, null);

        assertEquals(estudanteId, relatorio.studentId());
        assertEquals(NOME, relatorio.studentName());
        assertEquals(INICIO, relatorio.from());
        assertEquals(FIM, relatorio.to());
        assertNull(relatorio.schoolId());

        assertEquals(new BigDecimal("12.5"), relatorio.totalHours());
        assertEquals(400, relatorio.requiredHours());
        assertEquals(new BigDecimal("387.5"), relatorio.remainingHours());
        assertNull(relatorio.requirementMetAt());

        assertEquals(3, relatorio.performedLessons());
        assertEquals(new BigDecimal("2.5"), relatorio.validatedHoursInPeriod());
        assertEquals(new BigDecimal("0.5"), relatorio.pendingHoursInPeriod());
        assertEquals(1, relatorio.canceledLessons());
        assertEquals(1, relatorio.notCompletedLessons());

        assertEquals(4.0, relatorio.averageReceived());
        assertEquals(List.of(avaliacao), relatorio.evaluations());
        verify(tenancyValidator).validateStudentAccess(estudante);
    }

    @Test
    void deveLancarExcecaoQuandoEstudanteNaoExiste() {
        when(studentRepository.findById(estudanteId)).thenReturn(Optional.empty());

        UserNotFoundException erro = assertThrows(UserNotFoundException.class,
                () -> studentReportService.forStudent(estudanteId, INICIO, FIM, null));

        assertEquals("Estudante não encontrado!", erro.getMessage());
        verifyNoInteractions(tenancyValidator, reportLessonRepository, internshipFacade, feedbackFacade);
    }

    //quem nao tem vinculo com o estudante nao ve o relatorio dele.
    @Test
    void deveBarrarQuemNaoTemAcessoAoEstudante() {
        estudanteEncontrado();
        doThrow(new AccessDeniedException("Acesso negado: Você não tem acesso aos dados deste estudante."))
                .when(tenancyValidator).validateStudentAccess(estudante);

        assertThrows(AccessDeniedException.class,
                () -> studentReportService.forStudent(estudanteId, INICIO, FIM, null));

        verifyNoInteractions(reportLessonRepository, internshipFacade, feedbackFacade);
    }

    @Test
    void deveLancarExcecaoQuandoDataDeInicioForPosteriorAoTermino() {
        estudanteEncontrado();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> studentReportService.forStudent(estudanteId, FIM, INICIO, null));

        assertEquals("A data de início não pode ser posterior à data de término.", erro.getMessage());
        verifyNoInteractions(reportLessonRepository, internshipFacade, feedbackFacade);
    }

    @Test
    void deveLancarExcecaoQuandoEscolaDoFiltroNaoExiste() {
        UUID escolaInexistente = UUID.randomUUID();
        estudanteEncontrado();
        when(schoolRepository.existsById(escolaInexistente)).thenReturn(false);

        SchoolNotFoundException erro = assertThrows(SchoolNotFoundException.class,
                () -> studentReportService.forStudent(estudanteId, INICIO, FIM, escolaInexistente));

        assertEquals("Escola não encontrada!", erro.getMessage());
        verifyNoInteractions(reportLessonRepository, internshipFacade, feedbackFacade);
    }

    @Test
    void deveDevolverMediaNulaQuandoNaoHaAvaliacaoNoPeriodo() {
        relatorioCom(List.of(), List.of());

        StudentReportResponseDTO relatorio = studentReportService.forStudent(estudanteId, INICIO, FIM, null);

        assertNull(relatorio.averageReceived());
        assertTrue(relatorio.evaluations().isEmpty());
        assertEquals(0, relatorio.performedLessons());
        assertEquals(new BigDecimal("0.0"), relatorio.validatedHoursInPeriod());
        assertEquals(new BigDecimal("0.0"), relatorio.pendingHoursInPeriod());
    }

    //so entra na media a avaliacao de necessidade finalizada, dentro do periodo e da escola filtrada.
    @Test
    void deveConsiderarApenasAvaliacoesFinalizadasNoPeriodoEDaEscola() {
        StudentEvaluationResponseDTO primeira = avaliacao(escola.getId(), 4.0, dia(10));
        StudentEvaluationResponseDTO segunda = avaliacao(escola.getId(), 5.0, dia(31));
        StudentEvaluationResponseDTO naoFinalizada = avaliacao(escola.getId(), 1.0, null);
        StudentEvaluationResponseDTO foraDoPeriodo = avaliacao(escola.getId(), 1.0, dia(1).minusMonths(1));
        StudentEvaluationResponseDTO deOutraEscola = avaliacao(outraEscola.getId(), 1.0, dia(10));

        when(schoolRepository.existsById(escola.getId())).thenReturn(true);
        relatorioCom(List.of(),
                List.of(primeira, segunda, naoFinalizada, foraDoPeriodo, deOutraEscola));

        StudentReportResponseDTO relatorio =
                studentReportService.forStudent(estudanteId, INICIO, FIM, escola.getId());

        assertEquals(List.of(primeira, segunda), relatorio.evaluations());
        assertEquals(4.5, relatorio.averageReceived());
        assertEquals(escola.getId(), relatorio.schoolId());
    }

    @Test
    void deveContarApenasAulasDaEscolaInformada() {
        Lesson daEscola = comNineBox(aula(LessonStatus.VALIDATED, dia(3), 60, escola), 3, 3);
        Lesson deOutraEscola = comNineBox(aula(LessonStatus.VALIDATED, dia(4), 120, outraEscola), 1, 1);
        Lesson canceladaEmOutraEscola = aula(LessonStatus.CANCELED, dia(6), 60, outraEscola);

        when(schoolRepository.existsById(escola.getId())).thenReturn(true);
        relatorioCom(List.of(daEscola, deOutraEscola, canceladaEmOutraEscola), List.of());

        StudentReportResponseDTO relatorio =
                studentReportService.forStudent(estudanteId, INICIO, FIM, escola.getId());

        assertEquals(1, relatorio.performedLessons());
        assertEquals(new BigDecimal("1.0"), relatorio.validatedHoursInPeriod());
        assertEquals(0, relatorio.canceledLessons());
        //a carga do estagio continua inteira, o filtro nao mexe nela.
        assertEquals(new BigDecimal("12.5"), relatorio.totalHours());
    }

    @Test
    void deveConsultarComLimitesAbertosQuandoPeriodoNaoForInformado() {
        relatorioCom(List.of(), List.of());
        ArgumentCaptor<LocalDateTime> inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fim = ArgumentCaptor.forClass(LocalDateTime.class);

        StudentReportResponseDTO relatorio = studentReportService.forStudent(estudanteId, null, null, null);

        verify(reportLessonRepository).findByStudent_IdAndStatusInAndScheduledEndBetweenOrderByScheduledEndAsc(
                eq(estudanteId), situacoesConsultadas.capture(), inicio.capture(), fim.capture());
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0), inicio.getValue());
        assertEquals(LocalDateTime.of(2100, 1, 1, 0, 0), fim.getValue());
        assertEquals(4, situacoesConsultadas.getValue().size());
        assertTrue(situacoesConsultadas.getValue().containsAll(List.of(
                LessonStatus.COMPLETED, LessonStatus.VALIDATED,
                LessonStatus.CANCELED, LessonStatus.NOT_COMPLETED)));
        assertNull(relatorio.from());
        assertNull(relatorio.to());
    }

    @Test
    void deveDevolverSerieDoNineBoxApenasComAulasRealizadasEAvaliadas() {
        Lesson validada = comNineBox(aula(LessonStatus.VALIDATED, dia(2), 60, escola), 2, 3);
        Lesson pendente = comNineBox(aula(LessonStatus.COMPLETED, dia(9), 60, escola), 3, 1);
        Lesson semNineBox = aula(LessonStatus.COMPLETED, dia(10), 60, escola);
        Lesson soDesempenho = aula(LessonStatus.VALIDATED, dia(11), 60, escola);
        soDesempenho.setNineBoxPerformance(2);
        Lesson cancelada = comNineBox(aula(LessonStatus.CANCELED, dia(12), 60, escola), 1, 1);

        estudanteEncontrado();
        aulasEncontradas(List.of(validada, pendente, semNineBox, soDesempenho, cancelada));

        List<ClassProgressPointDTO> serie = studentReportService.classProgress(estudanteId, INICIO, FIM, null);

        assertEquals(2, serie.size());
        assertEquals(validada.getId(), serie.get(0).lessonId());
        assertEquals(validada.getScheduledEnd(), serie.get(0).lessonDate());
        assertEquals(2, serie.get(0).performance());
        assertEquals(3, serie.get(0).potential());
        assertEquals(pendente.getId(), serie.get(1).lessonId());
        verify(tenancyValidator).validateStudentAccess(estudante);
        //a serie nao precisa da carga nem das avaliacoes.
        verifyNoInteractions(internshipFacade, feedbackFacade);
    }

    @Test
    void deveFiltrarSerieDoNineBoxPelaEscolaInformada() {
        Lesson daEscola = comNineBox(aula(LessonStatus.VALIDATED, dia(3), 60, escola), 3, 2);
        Lesson deOutraEscola = comNineBox(aula(LessonStatus.VALIDATED, dia(4), 60, outraEscola), 1, 1);

        estudanteEncontrado();
        when(schoolRepository.existsById(escola.getId())).thenReturn(true);
        aulasEncontradas(List.of(daEscola, deOutraEscola));

        List<ClassProgressPointDTO> serie =
                studentReportService.classProgress(estudanteId, INICIO, FIM, escola.getId());

        assertEquals(1, serie.size());
        assertEquals(daEscola.getId(), serie.get(0).lessonId());
    }

    @Test
    void deveLancarExcecaoNaSerieQuandoEstudanteNaoExiste() {
        when(studentRepository.findById(estudanteId)).thenReturn(Optional.empty());

        UserNotFoundException erro = assertThrows(UserNotFoundException.class,
                () -> studentReportService.classProgress(estudanteId, INICIO, FIM, null));

        assertEquals("Estudante não encontrado!", erro.getMessage());
        verifyNoInteractions(reportLessonRepository);
    }
}
