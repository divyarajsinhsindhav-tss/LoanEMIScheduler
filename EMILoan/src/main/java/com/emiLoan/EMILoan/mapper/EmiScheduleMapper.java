package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmiScheduleMapper {

    @Mapping(target = "amountDue", expression = "java(calculateSecureAmountDue(emiSchedule))")
    EmiScheduleResponse toResponse(EmiSchedule emiSchedule);

    List<EmiScheduleResponse> toResponseList(List<EmiSchedule> emiSchedules);

    default java.math.BigDecimal calculateSecureAmountDue(EmiSchedule emi) {
        java.math.BigDecimal due = emi.getRemainingEmiDue();
        if (due == null || due.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return java.math.BigDecimal.ZERO.setScale(2);
        }
        return due.setScale(2);
    }
}