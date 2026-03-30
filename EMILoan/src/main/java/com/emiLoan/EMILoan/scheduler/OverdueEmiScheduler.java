package com.emiLoan.EMILoan.scheduler;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueEmiScheduler {

    private final EmiService emiService;
    private final EmiScheduleRepository emiScheduleRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "${app.scheduler.overdue-cron:0 0 1 * * ?}")
    @Transactional
    public void checkAndMarkOverdue() {
        LocalDate today = LocalDate.now();
        log.info(">>>> Starting Daily Overdue Detection Job for: {}", today);

        List<EmiSchedule> overdueCandidates = emiScheduleRepository
                .findByDueDateBeforeAndStatus(today, EmiStatus.PENDING);

        if (overdueCandidates.isEmpty()) {
            log.info("No new overdue EMIs detected today.");
            return;
        }

        log.info("Detected {} potential overdue installments. Processing...", overdueCandidates.size());

        for (EmiSchedule emi : overdueCandidates) {
            try {
                emi.setStatus(EmiStatus.OVERDUE);
                notificationService.sendOverdueAlert(emi.getLoan().getBorrower(), emi);

            } catch (Exception e) {
                log.error("Failed to process overdue alert for EMI ID: {}. Error: {}",
                        emi.getEmiId(), e.getMessage());
            }
        }

        emiScheduleRepository.saveAll(overdueCandidates);

        log.info("<<<< Overdue Detection Job Completed. Successfully processed {} records.",
                overdueCandidates.size());
    }
}