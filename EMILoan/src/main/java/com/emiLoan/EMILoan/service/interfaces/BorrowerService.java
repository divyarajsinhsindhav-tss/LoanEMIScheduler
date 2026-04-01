package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.user.response.BorrowerDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;

import java.math.BigDecimal;

public interface BorrowerService {
    BorrowerResponse getProfile();

    BorrowerResponse updateFinancialProfile(BigDecimal newMonthlyIncome);

    BorrowerDashboardResponse getDashboardStats();

    BorrowerResponse getProfileByUserCode(String userCode);
}

