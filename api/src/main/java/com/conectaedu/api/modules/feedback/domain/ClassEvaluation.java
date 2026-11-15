package com.conectaedu.api.modules.feedback.domain;

import com.conectaedu.api.modules.demand.domain.Demand;
import com.conectaedu.api.modules.user.student.domain.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "class_evaluation", uniqueConstraints = @UniqueConstraint(columnNames = {"demand_id", "student_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClassEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    //evolução da turma e infraestrutura da escola.

    private Integer engagement;
    private Integer progress;
    private Integer infrastructure;

    @Column(length = 2000)
    private String comments;

    private LocalDateTime createdAt;

    public String getAuditName() {
        return "Avaliação da sala de %s por %s".formatted(
                demand == null ? "Necessidade" : demand.getTitle(),
                student == null ? "estudante" : student.getName());
    }
}
