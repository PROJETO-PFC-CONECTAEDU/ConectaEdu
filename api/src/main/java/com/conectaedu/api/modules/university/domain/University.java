package com.conectaedu.api.modules.university.domain;

import com.conectaedu.api.shared.enums.UniversityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "universities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    //Identifica a intituição com base no CNPJ.
    @Column(name = "cnpj", nullable = false, length = 14, unique = true, updatable = false)
    private String cnpj;

    @Column(name = "coordinator", length = 200)
    private String coordinator;

    private String address;

    //Situação no fluxo de validação. Nasce PENDING.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UniversityStatus status = UniversityStatus.PENDING;

    //Nasce inativa e só é ativada após a validação do ADMIN.
    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validation_notes", length = 500)
    private String validationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}