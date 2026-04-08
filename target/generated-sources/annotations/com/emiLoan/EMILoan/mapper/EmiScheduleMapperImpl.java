package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T19:15:11+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class EmiScheduleMapperImpl implements EmiScheduleMapper {

    @Override
    public EmiScheduleResponse toResponse(EmiSchedule emiSchedule) {
        if ( emiSchedule == null ) {
            return null;
        }

        EmiScheduleResponse.EmiScheduleResponseBuilder emiScheduleResponse = EmiScheduleResponse.builder();

        emiScheduleResponse.emiId( emiSchedule.getEmiId() );
        emiScheduleResponse.installmentNo( emiSchedule.getInstallmentNo() );
        emiScheduleResponse.dueDate( emiSchedule.getDueDate() );
        emiScheduleResponse.emiCode( emiSchedule.getEmiCode() );
        emiScheduleResponse.principalComponent( emiSchedule.getPrincipalComponent() );
        emiScheduleResponse.interestComponent( emiSchedule.getInterestComponent() );
        emiScheduleResponse.totalEmi( emiSchedule.getTotalEmi() );
        emiScheduleResponse.remainingBalance( emiSchedule.getRemainingBalance() );
        emiScheduleResponse.status( emiSchedule.getStatus() );
        emiScheduleResponse.paidDate( emiSchedule.getPaidDate() );
        emiScheduleResponse.amountPaid( emiSchedule.getAmountPaid() );

        emiScheduleResponse.amountDue( calculateSecureAmountDue(emiSchedule) );

        return emiScheduleResponse.build();
    }
}
