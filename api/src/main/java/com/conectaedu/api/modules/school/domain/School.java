package com.conectaedu.api.modules.school.domain;

import com.conectaedu.api.shared.enums.SchoolStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schools")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    //Código identificador da escola.
    @Column(name = "CIE", nullable = false)
    private String cie;

    private String director;

    private String address;

    private double latitude;

    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SchoolStatus status = SchoolStatus.PENDING_ACTIVATION;

   @Column(name = "active", nullable = false)
   private boolean active = false;

   @Column(name = "validated_at")
   private LocalDateTime validatedAt;

   @Column(name = "validation_notes", length = 500)
   private String validationNotes;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
