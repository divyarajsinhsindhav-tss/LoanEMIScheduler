package com.emiLoan.EMILoan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrower_profile")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BorrowerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "borrower_id", updatable = false, nullable = false)
    private UUID borrowerId;

    @Column(name = "borrower_code", unique = true, insertable = false, updatable = false)
    private String borrowerCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "monthly_income", precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "existing_loan_count")
    @Builder.Default
    private Integer existingLoanCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
