package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;

public interface EmployeeService {

    /**
     * Retrieves the profile of the currently authenticated Loan Officer/Employee.
     * Uses the SecurityContext to identify the user.
     *
     * @return LoanOfficerResponse containing employee profile details
     */
    LoanOfficerResponse getProfile();
}