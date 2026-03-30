package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.service.interfaces.LoanApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan/applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;


    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> apply(
            @RequestBody @Valid LoanApplicationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LoanApplicationResponse response = loanApplicationService.apply(request, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Application " + response.getApplicationCode() + " submitted successfully.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping
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


    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplicationByCode(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LoanApplicationResponse response = loanApplicationService.getApplication(code, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Application details for code: " + code,
                httpServletRequest.getRequestURI(),
                response
        ));
    }


    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationDetailsResponse>> getApplicationDetails(
            @PathVariable UUID id,
            HttpServletRequest httpServletRequest
    ) {
        LoanApplicationDetailsResponse response = loanApplicationService.getById(id);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Detailed application view retrieved.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}