package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;
    private final EmiService emiService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        List<LoanResponse> response = loanService.getMyLoans(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Your loans retrieved successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetails(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanResponse response = loanService.getLoan(loanCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Loan details retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}/summary")
    public ResponseEntity<ApiResponse<LoanSummaryResponse>> getLoanSummary(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanSummaryResponse response = loanService.getLoanSummary(loanCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Loan summary retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}/schedule")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getSchedule(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        List<EmiScheduleResponse> response = emiService.getSchedule(loanCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Repayment schedule retrieved for loan: " + loanCode,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getAllLoans(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) LoanStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        Page<LoanResponse> response = loanService.getAllLoans(userDetails.getUsername(), pageNumber, pageSize, status);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Master loan directory retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PutMapping("/applications/{applicationCode}/decision")
    @PreAuthorize("hasAnyAuthority('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> processDecision(
            @PathVariable String applicationCode,
            @RequestBody @Valid OfficerDecisionRequest request,
            @AuthenticationPrincipal UserDetails officerDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanResponse response = loanService.processDecision(applicationCode, request, officerDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Application decision processed successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PatchMapping("/{loanCode}/status")
    @PreAuthorize("hasAnyAuthority('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponse>> updateLoanStatus(
            @PathVariable String loanCode,
            @RequestBody @Valid LoanStatusUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoanResponse response = loanService.updateLoanStatus(loanCode, request);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Loan status updated successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}