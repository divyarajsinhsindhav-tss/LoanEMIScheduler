package com.emiLoan.EMILoan.dto.payment.details;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NetBankingPaymentDetails extends PaymentMethodDetails {

    @NotBlank(message = "Bank code/name is required")
    private String bankCode;
}