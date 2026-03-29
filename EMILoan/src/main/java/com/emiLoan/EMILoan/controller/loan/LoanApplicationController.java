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
import org.springframework.web.bind.annotation.*;

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
        LoanApplicationResponse response = loanApplicationService.apply(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Loan application successfully created with " + response.getApplicationCode() + " ID",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<LoanApplicationDetailsResponse>>> getAllApplications(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) ApplicationStatus status,
            HttpServletRequest httpServletRequest
    ) {

        Page<LoanApplicationDetailsResponse> response =
                loanApplicationService.getApplications(pageNumber, pageSize, status);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(
                HttpStatus.OK,
                "Loan application successfully retrieved",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getMyApplication(
            @PathVariable String id,
            HttpServletRequest httpServletRequest
    ){
        LoanApplicationResponse response = loanApplicationService.getApplication(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(
                        HttpStatus.OK,
                        "Loan application: " + response.getApplicationCode(),
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }
}
