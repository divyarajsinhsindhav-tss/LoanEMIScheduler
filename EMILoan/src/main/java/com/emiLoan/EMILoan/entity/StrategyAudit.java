package com.emiLoan.EMILoan.entity;


import com.emiLoan.EMILoan.common.enums.LoanStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "strategy_audit")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StrategyAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_strategy")
    private LoanStrategy systemStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "officer_strategy")
    private LoanStrategy officerStrategy;

    @Builder.Default
    @Column(name = "overridden")
    private boolean overridden = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}