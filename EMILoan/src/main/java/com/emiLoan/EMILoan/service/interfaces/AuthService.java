package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.user.AuthResponse;
import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoginRequest;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;

public interface AuthService {


    AuthResponse login(LoginRequest request);

    UserResponse registerBorrower(BorrowerRegistrationRequest request);

    UserResponse registerLoanOfficer(LoanOfficerRegistrationRequest request);
}