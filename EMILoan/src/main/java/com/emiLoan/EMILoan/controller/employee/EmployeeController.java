package com.emiLoan.EMILoan.controller.employee;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import com.emiLoan.EMILoan.service.interfaces.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LoanOfficerResponse>> getProfile(HttpServletRequest request) {
        LoanOfficerResponse response = employeeService.getProfile();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Profile retrieved successfully",
                request.getRequestURI(),
                response));
    }

    @PostMapping("/admin/register/officer")
    public ResponseEntity<ApiResponse<UserResponse>> registerLoanOfficer(
            @Valid @RequestBody LoanOfficerRegistrationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = authService.registerLoanOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Loan Officer created successfully by Admin",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

}
