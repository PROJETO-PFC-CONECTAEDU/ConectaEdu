package com.conectaedu.api.modules.user.domain;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.shared.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("STUDENT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Student extends User {

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.PENDING;

    private String availability;

    private String course;

    private List<String> interestAreas;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
}
