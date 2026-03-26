package com.emiLoan.EMILoan.dto.emiSchedule.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoanScheduleWrapperResponse {
    private String loanCode;
    private String strategyName;
    private List<EmiScheduleResponse> schedule;
}