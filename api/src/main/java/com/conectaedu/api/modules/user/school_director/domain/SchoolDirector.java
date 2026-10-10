package com.conectaedu.api.modules.user.school_director.domain;

import com.conectaedu.api.modules.school.domain.School;
import com.conectaedu.api.modules.user.genericUser.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("SCHOOL_DIRECTOR")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SchoolDirector extends User {

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;
}
