package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.notification.NotificationResponse;
import com.emiLoan.EMILoan.dto.notification.NotificationSummaryResponse;
import com.emiLoan.EMILoan.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "loanId", source = "loan.loanId")
    @Mapping(target = "emiId", source = "emiSchedule.emiId")
    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "preview", source = "message", qualifiedByName = "createPreview")
    NotificationSummaryResponse toSummaryResponse(Notification notification);

    List<NotificationSummaryResponse> toSummaryList(List<Notification> notifications);

    @Named("createPreview")
    default String createPreview(String message) {
        if (message == null) return "";
        return message.length() > 50 ? message.substring(0, 47) + "..." : message;
    }
}
