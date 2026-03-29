package com.emiLoan.EMILoan.controller.loan;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan")
public class LoanController {

    private final LoanService loanService;
    private final EmiService emiService;

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getSchedule(
            @PathVariable String id,
            HttpServletRequest httpServletRequest
    ){
        List<EmiScheduleResponse> response = emiService.getSchedule(id);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(
                HttpStatus.OK,
                "Schedule of loan: " + id,
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> approve(
            @PathVariable String id,
            @RequestBody OfficerDecisionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoanResponse response = loanService.processDecision(id, request);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Loan application processed successfully",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}
