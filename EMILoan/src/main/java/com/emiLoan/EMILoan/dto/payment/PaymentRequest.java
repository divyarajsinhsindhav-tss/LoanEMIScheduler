package com.emiLoan.EMILoan.dto.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequest {

    @NotNull(message = "EMI ID is required")
    private UUID emiId;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode; // UPI, CARD, NETBANKING
}