package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmiScheduleMapper {

    @Mapping(target = "amountDue", expression = "java(calculateSecureAmountDue(emiSchedule))")
    EmiScheduleResponse toResponse(EmiSchedule emiSchedule);

    //List<EmiScheduleResponse> toResponseList(List<EmiSchedule> emiSchedules);

    default BigDecimal calculateSecureAmountDue(EmiSchedule emi) {
        if (emi.getTotalEmi() == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        BigDecimal paid = emi.getAmountPaid() != null ? emi.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal due = emi.getTotalEmi().subtract(paid);

        if (due.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return due.setScale(2);
    }
}