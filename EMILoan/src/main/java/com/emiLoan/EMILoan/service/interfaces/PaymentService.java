package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse makePayment(PaymentRequest request);

    List<PaymentHistoryResponse> getLoanPaymentHistory(String loanId);

    List<PaymentHistoryResponse> getBorrowerPaymentHistory();

    List<PaymentHistoryResponse> getAllPayments();
}