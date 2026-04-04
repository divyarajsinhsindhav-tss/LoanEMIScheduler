package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;
    private final EmiService emiService;


    @GetMapping("/")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getMyLoans(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanResponse> response = loanService.getMyLoans(userDetails.getUsername(),pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Your loans retrieved successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}")
    @PreAuthorize("hasAnyAuthority('BORROWER', 'LOAN_OFFICER', 'ADMIN')")
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
    @PreAuthorize("hasAuthority('BORROWER')")
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
    @PreAuthorize("hasAnyAuthority('BORROWER', 'LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<EmiScheduleResponse>>> getSchedule(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmiScheduleResponse> response = emiService.getSchedule(loanCode, userDetails.getUsername(),pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Repayment schedule retrieved for loan: " + loanCode,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}/schedule/next")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> getNextUpcomingEmi(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        EmiScheduleResponse response = emiService.getNextUpcomingEmi(loanCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Next upcoming EMI retrieved for loan: " + loanCode,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{loanCode}/foreclosure-quote")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getForeclosureQuote(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        BigDecimal response = emiService.getForeclosureQuote(loanCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Foreclosure quote calculated for loan: " + loanCode,
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


    @GetMapping("/{loanCode}/audit-history")
    @PreAuthorize("hasAnyAuthority('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLoanAuditHistory(
            @PathVariable String loanCode,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> response = loanService.getLoanAuditHistory(loanCode, userDetails.getUsername(),pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Audit history retrieved for loan: " + loanCode,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/strategy-overrides")
    @PreAuthorize("hasAnyAuthority('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<StrategyAuditResponse>>> getStrategyOverrides(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpServletRequest
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<StrategyAuditResponse> response = loanService.getStrategyOverrides(userDetails.getUsername(),pageable);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Strategy overrides retrieved successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}