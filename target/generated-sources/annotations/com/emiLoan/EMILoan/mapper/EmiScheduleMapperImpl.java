package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-04T11:16:37+0530",
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

    @Override
    public List<EmiScheduleResponse> toResponseList(List<EmiSchedule> emiSchedules) {
        if ( emiSchedules == null ) {
            return null;
        }

        List<EmiScheduleResponse> list = new ArrayList<EmiScheduleResponse>( emiSchedules.size() );
        for ( EmiSchedule emiSchedule : emiSchedules ) {
            list.add( toResponse( emiSchedule ) );
        }

        return list;
    }
}
