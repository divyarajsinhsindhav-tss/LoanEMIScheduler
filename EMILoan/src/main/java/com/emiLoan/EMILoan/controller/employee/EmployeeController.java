package com.emiLoan.EMILoan.controller.employee;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.UpdateEmployeeRequest;
import com.emiLoan.EMILoan.dto.user.response.EmployeeDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.dto.user.response.RegistrationResponse;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import com.emiLoan.EMILoan.service.interfaces.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthService authService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanOfficerResponse>> getProfile(HttpServletRequest request) {
        LoanOfficerResponse response = employeeService.getProfile();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Profile retrieved successfully",
                request.getRequestURI(),
                response));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getDashboardStats(HttpServletRequest request) {
        System.out.println("Current User Authorities: " +
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        EmployeeDashboardResponse response = employeeService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Operational dashboard metrics retrieved.",
                request.getRequestURI(),
                response));
    }

    @PostMapping("/admin/register/officer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerLoanOfficer(
            @Valid @RequestBody LoanOfficerRegistrationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        RegistrationResponse response = authService.registerLoanOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Loan Officer created successfully by Admin",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanOfficerResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Page<LoanOfficerResponse> response = employeeService.getAllEmployees(page, size);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Employee directory retrieved.",
                request.getRequestURI(),
                response));
    }

    @GetMapping("/admin/{userCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanOfficerResponse>> getEmployeeByCode(
            @PathVariable String userCode,
            HttpServletRequest request
    ) {
        LoanOfficerResponse response = employeeService.getEmployeeByUserCode(userCode);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Employee details retrieved for " + userCode,
                request.getRequestURI(),
                response));
    }

    @PatchMapping("/admin/{userCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanOfficerResponse>> updateEmployeeDetails(
            @PathVariable String userCode,
            @Valid @RequestBody UpdateEmployeeRequest updateRequest,
            HttpServletRequest request
    ) {
        LoanOfficerResponse response = employeeService.updateEmployeeDetails(userCode, updateRequest);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Employee records updated successfully.",
                request.getRequestURI(),
                response));
    }
}