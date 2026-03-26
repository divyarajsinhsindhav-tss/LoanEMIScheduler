package com.emiLoan.EMILoan.dto.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID paymentId;

    private String loanCode;
    private Integer installmentNo;

    private BigDecimal amountPaid;
    private LocalDateTime paymentDate;
    private PaymentMode paymentMode;
    private PaymentStatus status;
}