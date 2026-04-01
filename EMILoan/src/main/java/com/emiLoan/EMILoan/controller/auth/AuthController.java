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
        log.info("Login request: {}", request.getEmail());
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
        log.info("Register request: {}", request.getEmail());
        UserResponse response = authService.registerBorrower(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "Borrower registered successfully",
                        httpServletRequest.getRequestURI(),
                        response
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Logged out successfully. Please discard your token.",
                request.getRequestURI(),
                null
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
