package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;

import java.util.UUID;

public interface PaymentService{

    /**
     * Processes a payment for a specific EMI installment.
     */
    PaymentResponse makePayment(PaymentRequest request, String email);

    /**
     * Retrieves the complete transaction history for a specific loan.
     */
    PaymentHistoryResponse getPaymentHistory(UUID loanId, String email);
}