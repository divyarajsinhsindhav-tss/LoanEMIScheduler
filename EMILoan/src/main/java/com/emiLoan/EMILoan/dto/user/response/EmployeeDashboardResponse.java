package com.emiLoan.EMILoan.dto.user.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeDashboardResponse {
    private Long pendingApplicationsCount;
    private Long activeLoansCount;
    private Long overdueLoansCount;
}