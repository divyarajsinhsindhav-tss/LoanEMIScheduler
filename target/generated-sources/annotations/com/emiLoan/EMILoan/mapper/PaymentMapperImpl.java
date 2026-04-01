package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Payment;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-31T22:23:53+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public Payment toEntity(PaymentRequest request) {
        if ( request == null ) {
            return null;
        }

        Payment.PaymentBuilder payment = Payment.builder();

        payment.paymentMode( request.getPaymentMode() );

        return payment.build();
    }

    @Override
    public PaymentResponse toResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder paymentResponse = PaymentResponse.builder();

        paymentResponse.loanCode( paymentLoanLoanCode( payment ) );
        paymentResponse.installmentNo( paymentEmiScheduleInstallmentNo( payment ) );
        paymentResponse.paymentId( payment.getPaymentId() );
        paymentResponse.amountPaid( payment.getAmountPaid() );
        paymentResponse.paymentDate( payment.getPaymentDate() );
        paymentResponse.paymentMode( payment.getPaymentMode() );
        paymentResponse.status( payment.getStatus() );

        return paymentResponse.build();
    }

    @Override
    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        if ( payments == null ) {
            return null;
        }

        List<PaymentResponse> list = new ArrayList<PaymentResponse>( payments.size() );
        for ( Payment payment : payments ) {
            list.add( toResponse( payment ) );
        }

        return list;
    }

    private String paymentLoanLoanCode(Payment payment) {
        Loan loan = payment.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getLoanCode();
    }

    private Integer paymentEmiScheduleInstallmentNo(Payment payment) {
        EmiSchedule emiSchedule = payment.getEmiSchedule();
        if ( emiSchedule == null ) {
            return null;
        }
        return emiSchedule.getInstallmentNo();
    }
}
