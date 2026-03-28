package com.emiLoan.EMILoan.entity;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "loan_id", updatable = false, nullable = false)
    private UUID loanId;

    @Column(name = "loan_code", unique = true, insertable = false, updatable = false)
    private String loanCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LoanApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User borrower;

    @Column(name = "principal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "strategy", length = 50)
    private String strategy;

    @Column(name = "emi_amount", precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", length = 20)
    @Builder.Default
    private LoanStatus loanStatus = LoanStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}