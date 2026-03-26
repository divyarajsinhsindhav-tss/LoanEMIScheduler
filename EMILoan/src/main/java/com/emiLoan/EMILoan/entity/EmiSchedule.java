package com.emiLoan.EMILoan.entity;


import com.emiLoan.EMILoan.common.enums.EmiStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "emi_schedule")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "emi_id", updatable = false, nullable = false)
    private UUID emiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_component", nullable = false, precision = 12, scale = 2)
    private BigDecimal principalComponent;

    @Column(name = "interest_component", nullable = false, precision = 12, scale = 2)
    private BigDecimal interestComponent;

    @Column(name = "total_emi", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEmi;

    @Column(name = "remaining_balance", precision = 12, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmiStatus status = EmiStatus.PENDING;

    @Column(name = "paid_date")
    private LocalDate paidDate;
}