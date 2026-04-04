package com.emiLoan.EMILoan.dto.user.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BorrowerDashboardResponse {
    private Long activeLoanCount;
    private BigDecimal totalOutstandingAmount;
    private Integer upcomingPaymentsCount;
}