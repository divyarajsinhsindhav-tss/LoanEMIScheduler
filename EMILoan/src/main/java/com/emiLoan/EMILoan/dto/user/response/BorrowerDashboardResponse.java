package com.emiLoan.EMILoan.dto.user.response;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BorrowerDashboardResponse {
    private Long activeLoanCount;
    private BigDecimal totalOutstandingAmount;
    private Integer upcomingPaymentsCount;
}