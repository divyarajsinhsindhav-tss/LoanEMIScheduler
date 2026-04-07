package com.emiLoan.EMILoan.scheduler;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.emiLoan.EMILoan.common.constants.AppConstants.REMINDER_DAYS_BEFORE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final EmiScheduleRepository emiScheduleRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(REMINDER_DAYS_BEFORE);

        log.info(">>>> Starting Daily Payment Reminder Job for due date: {}", targetDate);
        List<EmiSchedule> upcomingEmis = emiScheduleRepository
                .findByStatusAndDueDateBetween(EmiStatus.PENDING, today, targetDate);

        if (upcomingEmis.isEmpty()) {
            log.info("No upcoming payments found for reminder today.");
            return;
        }

        log.info("Sending reminders to {} borrowers...", upcomingEmis.size());

        int successCount = 0;
        for (EmiSchedule emi : upcomingEmis) {
            try {
                notificationService.sendPaymentReminder(emi.getLoan().getBorrower(), emi);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send reminder for EMI ID: {}. Error: {}",
                        emi.getEmiId(), e.getMessage());
            }
        }

        log.info("<<<< Payment Reminder Job Completed. Sent {} reminders.", successCount);
    }
}
