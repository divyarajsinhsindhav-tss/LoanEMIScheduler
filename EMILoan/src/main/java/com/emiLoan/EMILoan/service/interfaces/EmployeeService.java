package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.dto.user.request.UpdateEmployeeRequest;
import com.emiLoan.EMILoan.dto.user.response.EmployeeDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import org.springframework.data.domain.Page;

public interface EmployeeService {

    LoanOfficerResponse getProfile();

    EmployeeDashboardResponse getDashboardStats();

    Page<LoanOfficerResponse> getAllEmployees(int page, int size);

    LoanOfficerResponse getEmployeeByUserCode(String userCode);

    LoanOfficerResponse updateEmployeeDetails(String userCode, UpdateEmployeeRequest request);

}