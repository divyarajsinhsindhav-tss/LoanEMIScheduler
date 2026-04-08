package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationSubmitResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationWithdrawResponse;
import com.emiLoan.EMILoan.service.interfaces.LoanApplicationService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan/applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<LoanApplicationSubmitResponse>> apply(
            @RequestBody @Valid LoanApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanApplicationSubmitResponse response = loanApplicationService.apply(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Application " + response.getApplicationCode() + " submitted successfully.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BORROWER', 'LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanApplicationDetailsResponse>>> getApplications(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) ApplicationStatus status,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        Page<LoanApplicationDetailsResponse> response =
                loanApplicationService.getApplications(userDetails.getUsername(), pageNumber, pageSize, status);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Applications retrieved successfully.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{applicationCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplication(
            @PathVariable String applicationCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanApplicationResponse response = loanApplicationService.getApplication(applicationCode, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Application details for code: " + applicationCode,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{applicationCode}/details")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanApplicationDetailsResponse>> getApplicationDetails(
            @PathVariable String applicationCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanApplicationDetailsResponse response = loanApplicationService.getByApplicationCode(applicationCode);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Detailed application view retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PatchMapping("/{applicationCode}/withdraw")
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<LoanApplicationWithdrawResponse>> withdrawApplication(
            @PathVariable String applicationCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        LoanApplicationWithdrawResponse response = loanApplicationService.withdrawApplication(applicationCode, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "Application withdrawn successfully", request.getRequestURI(), response));
    }
}