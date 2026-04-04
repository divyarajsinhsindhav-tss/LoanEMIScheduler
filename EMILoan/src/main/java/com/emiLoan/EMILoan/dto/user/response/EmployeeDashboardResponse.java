package com.emiLoan.EMILoan.dto.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDashboardResponse {
    private Long pendingApplicationsCount;
    private Long activeLoansCount;
    private Long overdueLoansCount;
}