package com.conectaedu.api.modules.user.university_admin.domain;

import com.conectaedu.api.modules.university.domain.University;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("UNIVERSITY_ADMIN")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UniversityAdmin extends User {
    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;
}
