package com.conectaedu.api.modules.demand.domain;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.user.school_director.domain.SchoolDirector;
import com.conectaedu.api.modules.user.student.domain.Student;
import com.conectaedu.api.shared.enums.DemandStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "demand")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Demand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;
    private String subject;
    private String gradeLevel;
    private Integer pupilAmount;
    private String room;
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    private DemandStatus status;

    private LocalDateTime classDate;
    private String totalHours;

    private LocalDateTime pendingClassDate;
    private String pendingRoom;

    private boolean isArchived = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandStudent> studentHistory = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "demand_candidates",
        joinColumns = @JoinColumn(name = "demand_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> candidates = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "director_id", nullable = false)
    private SchoolDirector director;
}
