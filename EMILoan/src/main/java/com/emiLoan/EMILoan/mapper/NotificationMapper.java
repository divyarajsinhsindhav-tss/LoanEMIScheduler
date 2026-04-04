package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.notification.NotificationResponse;
import com.emiLoan.EMILoan.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "loanId", source = "loan.loanId")
    @Mapping(target = "emiId", source = "emiSchedule.emiId")
    NotificationResponse toResponse(Notification notification);


    List<NotificationResponse> toResponseList(List<Notification> notifications);


}