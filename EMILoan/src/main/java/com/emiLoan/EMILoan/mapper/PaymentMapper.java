package com.emiLoan.EMILoan.mapper;


import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.entity.Payment;
import com.emiLoan.EMILoan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "emiSchedule", ignore = true)
    @Mapping(target = "loan", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "amountPaid", ignore = true)
    Payment toEntity(PaymentRequest request);

    @Mapping(target = "loanCode", source = "loan.loanCode")
    @Mapping(target = "installmentNo", source = "emiSchedule.installmentNo")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    @Mapping(target = "loanCode", source = "loan.loanCode")
    @Mapping(target = "totalAmountPaid", source = "totalAmountPaid")
    @Mapping(target = "transactions", source = "transactions")
    PaymentHistoryResponse toHistoryResponse(Loan loan, BigDecimal totalAmountPaid, List<PaymentResponse> transactions);
}
