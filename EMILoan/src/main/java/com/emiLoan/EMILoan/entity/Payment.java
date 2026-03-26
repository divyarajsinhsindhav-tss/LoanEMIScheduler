package com.emiLoan.EMILoan.entity;


import com.emiLoan.EMILoan.common.enums.PaymentMode;

import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_id", updatable = false, nullable = false)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_id", nullable = false)
    private EmiSchedule emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "payment_date", updatable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
    }
}