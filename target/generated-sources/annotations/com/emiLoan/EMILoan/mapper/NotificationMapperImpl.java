package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.notification.NotificationResponse;
import com.emiLoan.EMILoan.dto.notification.NotificationSummaryResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Notification;
import com.emiLoan.EMILoan.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T18:41:18+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponse.NotificationResponseBuilder notificationResponse = NotificationResponse.builder();

        notificationResponse.userId( notificationUserUserId( notification ) );
        notificationResponse.email( notification.getEmail() );
        notificationResponse.loanId( notificationLoanLoanId( notification ) );
        notificationResponse.emiId( notificationEmiScheduleEmiId( notification ) );
        notificationResponse.notificationId( notification.getNotificationId() );
        notificationResponse.subject( notification.getSubject() );
        notificationResponse.message( notification.getMessage() );
        notificationResponse.sentAt( notification.getSentAt() );
        notificationResponse.status( notification.getStatus() );

        return notificationResponse.build();
    }

    @Override
    public NotificationSummaryResponse toSummaryResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationSummaryResponse notificationSummaryResponse = new NotificationSummaryResponse();

        notificationSummaryResponse.setPreview( createPreview( notification.getMessage() ) );
        notificationSummaryResponse.setSubject( notification.getSubject() );
        notificationSummaryResponse.setSentAt( notification.getSentAt() );
        notificationSummaryResponse.setStatus( notification.getStatus() );

        return notificationSummaryResponse;
    }

    @Override
    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if ( notifications == null ) {
            return null;
        }

        List<NotificationResponse> list = new ArrayList<NotificationResponse>( notifications.size() );
        for ( Notification notification : notifications ) {
            list.add( toResponse( notification ) );
        }

        return list;
    }

    @Override
    public List<NotificationSummaryResponse> toSummaryList(List<Notification> notifications) {
        if ( notifications == null ) {
            return null;
        }

        List<NotificationSummaryResponse> list = new ArrayList<NotificationSummaryResponse>( notifications.size() );
        for ( Notification notification : notifications ) {
            list.add( toSummaryResponse( notification ) );
        }

        return list;
    }

    private UUID notificationUserUserId(Notification notification) {
        User user = notification.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUserId();
    }

    private UUID notificationLoanLoanId(Notification notification) {
        Loan loan = notification.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getLoanId();
    }

    private UUID notificationEmiScheduleEmiId(Notification notification) {
        EmiSchedule emiSchedule = notification.getEmiSchedule();
        if ( emiSchedule == null ) {
            return null;
        }
        return emiSchedule.getEmiId();
    }
}
