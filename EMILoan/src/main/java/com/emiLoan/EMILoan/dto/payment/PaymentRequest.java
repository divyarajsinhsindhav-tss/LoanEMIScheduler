package com.emiLoan.EMILoan.dto.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.dto.payment.details.PaymentMethodDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {

    @NotNull(message = "EMI ID is required")
    private UUID emiId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Minimum payment amount is ₹1.00")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentMethodDetails methodDetails;
}