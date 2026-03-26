package com.emiLoan.EMILoan.entity;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "action_time", updatable = false)
    private LocalDateTime actionTime;

    @PrePersist
    protected void onCreate() {
        this.actionTime = LocalDateTime.now();
    }
}