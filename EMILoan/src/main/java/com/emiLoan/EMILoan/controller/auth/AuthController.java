package com.emiLoan.EMILoan.controller.auth;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.AuthResponse;
import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoginRequest;
import com.emiLoan.EMILoan.dto.user.request.VerifyOtpRequest;
import com.emiLoan.EMILoan.dto.user.response.RegistrationResponse;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                response.getMessage(),
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PostMapping("/register/borrower")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerBorrower(
            @Valid @RequestBody BorrowerRegistrationRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("New borrower registration request for email: {}", request.getEmail());

        RegistrationResponse response = authService.registerBorrower(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Registration initiated successfully! Please check your email for the OTP to verify your account.",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<ApiResponse<RegistrationResponse>> verifyRegistration(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Verifying registration OTP for: {}", request.getEmail());

        RegistrationResponse response = authService.verifyRegistrationOtp(request.getEmail(), request.getOtpCode());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                response.getMessage(),
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @PostMapping("/verify-login")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLogin(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Verifying login OTP for: {}", request.getEmail());
        AuthResponse response = authService.verifyLoginOtp(request.getEmail(), request.getOtpCode());

        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "2FA Verification successful. Welcome back!",
                httpServletRequest.getRequestURI(),
                response
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Profile retrieved",
                request.getRequestURI(),
                response
        ));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestParam String email,
            HttpServletRequest request
    ) {
        authService.deleteUser(email);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Account " + email + " has been removed from the system",
                request.getRequestURI(),
                null
        ));
    }

    @PostMapping("/recover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> recoverAccount(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthResponse response = authService.recoverAccount(request);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Account successfully recovered and logged in.",
                httpServletRequest.getRequestURI(),
                response
        ));
    }
}
