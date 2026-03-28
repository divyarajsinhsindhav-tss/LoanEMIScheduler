package com.emiLoan.EMILoan.entity;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE loan_applications SET is_deleted = true, deleted_at = NOW() WHERE application_id = ?")
@SQLRestriction("is_deleted = false")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "application_id", updatable = false, nullable = false)
    private UUID applicationId;

    @Column(name = "application_code", unique = true, insertable = false, updatable = false)
    private String applicationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User borrower;

    @Column(name = "requested_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "existing_emi", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal existingEmi = BigDecimal.ZERO;

    @Column(name = "dti_ratio", precision = 5, scale = 2)
    private BigDecimal dtiRatio;

    @Column(name = "suggested_strategy", length = 50)
    private String suggestedStrategy;

    @Column(name = "officer_strategy", length = 50)
    private String officerStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Builder.Default
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
    }
}