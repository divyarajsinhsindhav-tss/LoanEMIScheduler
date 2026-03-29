package com.emiLoan.EMILoan.controller.payment;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.service.interfaces.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

        private final PaymentService paymentService;

        @GetMapping("/loan/{loanId}")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllPayments(
                        @PathVariable String loanId,
                        HttpServletRequest httpServletRequest) {
                List<PaymentHistoryResponse> responses = paymentService.getLoanPaymentHistory(loanId);

                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(
                                HttpStatus.OK,
                                "Payment History of all your emi of loan " + loanId,
                                httpServletRequest.getRequestURI(),
                                responses));
        }

        @GetMapping("/")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllPayment(
                        HttpServletRequest httpServletRequest) {
                List<PaymentHistoryResponse> responses = paymentService.getBorrowerPaymentHistory();

                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(
                                HttpStatus.OK,
                                "Payment History of all your emi",
                                httpServletRequest.getRequestURI(),
                                responses));
        }

        @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllPayments(
                        HttpServletRequest httpServletRequest) {
                List<PaymentHistoryResponse> responses = paymentService.getAllPayments();
                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(
                                HttpStatus.OK,
                                "Complete History of payments",
                                httpServletRequest.getRequestURI(),
                                responses));
        }

        @PostMapping("/pay")
        public ResponseEntity<ApiResponse<PaymentResponse>> payment(
                        HttpServletRequest httpServletRequest,
                        @RequestBody PaymentRequest request) {
                PaymentResponse paymentResponse = paymentService.makePayment(request);

                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                                HttpStatus.CREATED,
                                "Payment Successfully created",
                                httpServletRequest.getRequestURI(),
                                paymentResponse));
        }
}
