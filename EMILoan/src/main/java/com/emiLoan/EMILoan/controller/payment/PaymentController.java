package com.emiLoan.EMILoan.controller.payment;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.service.interfaces.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

        private final PaymentService paymentService;


        @PostMapping("/pay")
        public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(
                @RequestBody @Valid PaymentRequest request,
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest) {

                // Pass the authenticated email to the service for security validation
                PaymentResponse response = paymentService.makePayment(request, userDetails.getUsername());

                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Payment processed successfully. Status: " + response.getStatus(),
                        httpServletRequest.getRequestURI(),
                        response));
        }

        @GetMapping("/loan/{loanCode}")
        public ResponseEntity<ApiResponse<PaymentHistoryResponse>> getLoanPaymentHistory(
                @PathVariable String loanCode,
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest) {

                PaymentHistoryResponse response = paymentService.getPaymentHistory(loanCode, userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Payment history for loan: " + response.getLoanCode(),
                        httpServletRequest.getRequestURI(),
                        response));
        }


        @GetMapping("/history")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getMyPaymentHistory(
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest) {

                List<PaymentHistoryResponse> responses = paymentService.getBorrowerPaymentHistory(userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Complete personal payment history retrieved.",
                        httpServletRequest.getRequestURI(),
                        responses));
        }

        @GetMapping("/all")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllSystemPayments(
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest) {

                List<PaymentHistoryResponse> responses = paymentService.getAllPayments(userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Master payment records retrieved.",
                        httpServletRequest.getRequestURI(),
                        responses));
        }
}