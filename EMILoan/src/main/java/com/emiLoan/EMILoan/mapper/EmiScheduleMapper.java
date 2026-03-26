package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.dto.emiSchedule.response.LoanScheduleWrapperResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmiScheduleMapper {

    EmiScheduleResponse toResponse(EmiSchedule emiSchedule);

    List<EmiScheduleResponse> toResponseList(List<EmiSchedule> emiSchedules);

    @Mapping(target = "loanCode", source = "loan.loanCode")
    @Mapping(target = "strategyName", source = "loan.strategy")
    @Mapping(target = "schedule", source = "emiSchedules")
    LoanScheduleWrapperResponse toWrapperResponse(Loan loan, List<EmiSchedule> emiSchedules);
}