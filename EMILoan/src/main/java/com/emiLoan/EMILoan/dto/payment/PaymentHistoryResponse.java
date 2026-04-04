package com.emiLoan.EMILoan.dto.payment;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentHistoryResponse {
    private String loanCode;
    private BigDecimal totalAmountPaid;
    private List<PaymentResponse> transactions;
}