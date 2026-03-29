package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;

import java.util.UUID;

public interface PaymentService{

    PaymentResponse makePayment(PaymentRequest request, String email);


    PaymentHistoryResponse getPaymentHistory(UUID loanId, String email);
}