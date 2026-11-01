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

//sem updatedAt e sem status, a avaliação nasce e não muda mais.

@Entity
@Table(name = "student_evaluation", uniqueConstraints = @UniqueConstraint(columnNames = {"demand_id", "student_id", "evaluated_by"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand;

    //a necessidade pode trocar de estudante no meio do ciclo.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    //o diretor que avaliou.

    @Column(name = "evaluated_by", nullable = false)
    private UUID evaluatedBy;

    private Integer knowledge;

    private Integer punctuality;

    private Integer teaching;

    private Integer commitment;

    private Integer empathy;

    @Column(length = 2000)
    private String comments;

    private LocalDateTime createdAt;

    public String getAuditName() {
        return "Avaliação de %s em %s".formatted(
                student == null ? "estudante" : student.getName(),
                demand == null ? "Necessidade" : demand.getTitle());
    }
}
