package com.emiLoan.EMILoan.controller.auth;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.AuthResponse;
import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoginRequest;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Login successful",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PostMapping("/register/borrower")
    public ResponseEntity<ApiResponse<UserResponse>> registerBorrower(
            @Valid @RequestBody BorrowerRegistrationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = authService.registerBorrower(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Borrower registered successfully",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }
}
