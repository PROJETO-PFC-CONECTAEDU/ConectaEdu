package com.conectaedu.api.modules.lesson.domain;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.enums.CancellationReason;
import com.conectaedu.api.shared.enums.LessonOrigin;
import com.conectaedu.api.shared.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lesson")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //A demanda pode trocar de disciplina depois. Se a aula lesse de lá, o histórico mudaria junto.

    private String subject;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    private LessonStatus status = LessonStatus.SCHEDULED;

    //O diretor declara no agendamento se a aula é nova ou reposição.

    @Enumerated(EnumType.STRING)
    private LessonOrigin origin = LessonOrigin.NEW;

    private int enrolledCount;

    //Nulo até o estudante de pedagogia registrar a aula.
    private Integer presentCount;

    @Column(length = 2000)
    private String contentTaught;

    //Nine Box da turma, de 1 a 3 em cada eixo, preenchido pelo estudante no registro.

    private Integer nineBoxPerformance;
    private Integer nineBoxPotential;

    @Enumerated(EnumType.STRING)
    private CancellationReason cancellationReason;

    @Column(length = 500)
    private String cancellationNotes;

    private LocalDateTime confirmedAt;
    private LocalDateTime registeredAt;
    private LocalDateTime signedAt;

    //Diretor da escola, ou administrador da universidade depois do escalonamento.

    private UUID signedBy;

    //As duas rotinas de assinatura precisam saber se já agiram nesta aula, caso contrario cobram
    //o diretor em toda execução.

    private LocalDateTime reminderSentAt;
    private LocalDateTime escalatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    //A duração nunca é digitada manualmente, é sempre a diferença entre o fim e o início.
    @Transient
    public Duration getDuration() {
        return Duration.between(scheduledStart, scheduledEnd);
    }

    //A aula não tem título como a demanda, então o log de auditoria usa disciplina e data.
    @Transient
    public String getAuditName() {
        return "%s de %s".formatted(subject == null ? "Aula" : subject, scheduledStart);
    }
}