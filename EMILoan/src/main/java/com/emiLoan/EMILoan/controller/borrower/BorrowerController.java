package com.emiLoan.EMILoan.controller.borrower;

import com.emiLoan.EMILoan.common.response.ApiResponse;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.services.borrower.BorrowerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/borrower")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<BorrowerResponse>> getProfile(HttpServletRequest request) {
        BorrowerResponse response = borrowerService.getProfile();
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK,
                "Profile retrieved successfully",
                request.getRequestURI(),
                response));
    }

}
