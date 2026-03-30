package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse makePayment(PaymentRequest request,String email);

    List<PaymentHistoryResponse> getLoanPaymentHistory(String loanId);
    List<PaymentHistoryResponse> getBorrowerPaymentHistory(String borrowerEmail);

    List<PaymentHistoryResponse> getAllPayments(String email);

    PaymentHistoryResponse getPaymentHistory(String loanCode, String requesterEmail);
}