package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
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
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;
    private final EmiService emiService;

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getSchedule(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        // Service expects UUID and Email for security checks
        List<EmiScheduleResponse> response = emiService.getSchedule(id, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Repayment schedule retrieved for loan: " + id,
                httpServletRequest.getRequestURI(),
                response
        ));
    }


    @PostMapping("/applications/{appId}/decision")
    public ResponseEntity<ApiResponse<LoanResponse>> processDecision(
            @PathVariable UUID appId,
            @RequestBody @Valid OfficerDecisionRequest request,
            @AuthenticationPrincipal UserDetails officerDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanResponse response = loanService.processDecision(appId, request, officerDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Application decision processed successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<LoanSummaryResponse>> getLoanSummary(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanSummaryResponse response = loanService.getLoanSummary(id, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Loan summary retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}