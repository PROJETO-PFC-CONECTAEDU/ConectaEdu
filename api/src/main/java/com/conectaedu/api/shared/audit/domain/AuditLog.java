package com.conectaedu.api.shared.audit.domain;

import com.conectaedu.api.shared.enums.AuditAction;
import com.conectaedu.api.shared.enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private AuditEntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDate occurredAt;

    public AuditLog(AuditEntityType entityType, UUID entityId,
                    AuditAction action, String description, String performedBy) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.description = description;
        this.performedBy = performedBy;
        this.occurredAt = LocalDate.now();
    }
}
