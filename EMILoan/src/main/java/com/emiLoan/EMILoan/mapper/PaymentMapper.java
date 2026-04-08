package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "emiSchedule", ignore = true)
    @Mapping(target = "loan", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "amountPaid", source = "amount")
    Payment toEntity(PaymentRequest request);

    @Mapping(target = "loanCode", source = "loan.loanCode")
    @Mapping(target = "emiCode", source = "emiSchedule.emiCode")
    @Mapping(target = "installmentNo", source = "emiSchedule.installmentNo")
    PaymentResponse toResponse(Payment payment);


}