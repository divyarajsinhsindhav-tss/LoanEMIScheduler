package com.emiLoan.EMILoan.controller.borrower;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.response.BorrowerDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.service.interfaces.BorrowerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/borrower")
@Validated
public class BorrowerController {

    private final BorrowerService borrowerService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<BorrowerResponse>> getProfile(HttpServletRequest httpServletRequest) {
        BorrowerResponse response = borrowerService.getProfile();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Profile retrieved successfully",
                httpServletRequest.getRequestURI(),
                response));
    }

    @PatchMapping("/profile/income")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<BorrowerResponse>> updateIncome(
            @RequestParam
            @NotNull(message = "Income cannot be null")
            @DecimalMin(value = "1.0", message = "Income must be greater than zero")
            BigDecimal newMonthlyIncome,
            HttpServletRequest httpServletRequest
    ) {
        BorrowerResponse response = borrowerService.updateFinancialProfile(newMonthlyIncome);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Financial profile updated successfully",
                httpServletRequest.getRequestURI(),
                response));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('BORROWER')")
    public ResponseEntity<ApiResponse<BorrowerDashboardResponse>> getDashboardStats(HttpServletRequest httpServletRequest) {
        BorrowerDashboardResponse response = borrowerService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Dashboard statistics retrieved successfully",
                httpServletRequest.getRequestURI(),
                response));
    }

    @GetMapping("/profile/{userCode}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<BorrowerResponse>> getProfileByUserCode(
            @PathVariable String userCode,
            HttpServletRequest httpServletRequest
    ) {
        BorrowerResponse response = borrowerService.getProfileByUserCode(userCode);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Borrower profile retrieved for user code: " + userCode,
                httpServletRequest.getRequestURI(),
                response));
    }
}