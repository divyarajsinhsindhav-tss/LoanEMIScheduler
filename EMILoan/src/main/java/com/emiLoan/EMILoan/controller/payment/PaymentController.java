package com.emiLoan.EMILoan.controller.payment;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.payment.ForeclosureRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.service.interfaces.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

        private final PaymentService paymentService;


        @PostMapping("/pay")
        @PreAuthorize("hasRole('BORROWER')")
        public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(
                @Valid @RequestBody PaymentRequest request,
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest
        ) {
                PaymentResponse response = paymentService.makePayment(request, userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Payment processed successfully",
                        httpServletRequest.getRequestURI(),
                        response
                ));
        }

        @PostMapping("/foreclose")
        @PreAuthorize("hasRole('BORROWER')")
        public ResponseEntity<ApiResponse<PaymentResponse>> forecloseLoan(
                @Valid @RequestBody ForeclosureRequest request,
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest
        ) {
                PaymentResponse response = paymentService.forecloseLoan(request, userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Foreclosure payment processed and loan closed.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
        }


        @GetMapping("/history/my")
        @PreAuthorize("hasRole('BORROWER')")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getMyPaymentHistory(
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
                ) {
                Pageable pageable = PageRequest.of(page, size);
                List<PaymentHistoryResponse> response = paymentService.getBorrowerPaymentHistory(userDetails.getUsername(),pageable);

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Your payment history retrieved successfully.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
        }

        @GetMapping("/history/loan/{loanCode}")
        @PreAuthorize("hasAnyRole('BORROWER', 'LOAN_OFFICER', 'ADMIN')")
        public ResponseEntity<ApiResponse<PaymentHistoryResponse>> getLoanPaymentHistory(
                @PathVariable String loanCode,
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest
        ) {
                PaymentHistoryResponse response = paymentService.getPaymentHistory(loanCode, userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Payment history retrieved for loan: " + loanCode,
                        httpServletRequest.getRequestURI(),
                        response
                ));
        }

        @GetMapping("/history/all")
        @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
        public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllPayments(
                @AuthenticationPrincipal UserDetails userDetails,
                HttpServletRequest httpServletRequest
        ) {
                List<PaymentHistoryResponse> response = paymentService.getAllPayments(userDetails.getUsername());

                return ResponseEntity.ok(ApiResponse.of(
                        HttpStatus.OK,
                        "Master payment ledger retrieved.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
        }
}