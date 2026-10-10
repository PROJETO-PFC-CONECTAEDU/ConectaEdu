package com.conectaedu.api.modules.demand.domain;

import com.conectaedu.api.modules.user.student.domain.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "demand_student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id")
    private Demand demand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private boolean active;
    
    private LocalDateTime linkedAt;
    
    private LocalDateTime unlinkedAt;
}
