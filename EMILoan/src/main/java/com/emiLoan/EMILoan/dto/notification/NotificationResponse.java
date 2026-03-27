package com.emiLoan.EMILoan.dto.notification;


import com.emiLoan.EMILoan.common.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID notificationId;

    private UUID userId;
    private String email;
    private UUID loanId;
    private UUID emiId;

    private String subject;
    private String message;

    private LocalDateTime sentAt;
    private NotificationStatus status;
}