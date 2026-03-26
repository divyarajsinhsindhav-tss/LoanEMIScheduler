package com.emiLoan.EMILoan.dto.notification;


import com.emiLoan.EMILoan.common.enums.NotificationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationSummaryResponse {
    private String subject;
    private LocalDateTime sentAt;
    private NotificationStatus status;

    private String preview;
}