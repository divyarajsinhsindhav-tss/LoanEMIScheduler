package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.dto.emiSchedule.response.EmiScheduleResponse;
import com.emiLoan.EMILoan.engine.AmortizationEngine;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.EmiScheduleMapper;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmiServiceImpl implements EmiService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private final AmortizationEngine amortizationEngine;
    private final EmiScheduleMapper emiScheduleMapper;

    private final NotificationService notificationService;
    private final AuditService auditService;


    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponse> getSchedule(String loanCode, String requesterEmail) {
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan record not found for ID: " + loanCode));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user session invalid."));

        List<EmiSchedule> scheduleList = emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan);
        return emiScheduleMapper.toResponseList(scheduleList);
    }


    @Override
    @Transactional
    public void generateAndSaveSchedule(Loan loan) {
        if (!emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan).isEmpty()) {
            log.warn("Schedule skipped: Loan {} already has an active EMI schedule.", loan.getLoanCode());
            return;
        }

        List<EmiSchedule> schedules = amortizationEngine.buildSchedule(loan);

        if (schedules == null || schedules.isEmpty()) {
            throw new BusinessRuleException("Critical Engine Failure: Could not generate schedule for " + loan.getLoanCode());
        }

        emiScheduleRepository.saveAll(schedules);

        BigDecimal baseEmi = schedules.get(0).getTotalEmi();
        loan.setEmiAmount(baseEmi);
        loanRepository.save(loan);

        log.info("Financial Event: Generated {} installments for Loan {}", schedules.size(), loan.getLoanCode());
    }


    @Override
    @Transactional
    public void processOverdueEmis(LocalDate currentDate) {
        log.info("Batch Job Started: Identifying overdue installments for {}", currentDate);

        List<EmiSchedule> overdueList = emiScheduleRepository.findByDueDateBeforeAndStatus(currentDate, EmiStatus.PENDING);

        if (overdueList.isEmpty()) {
            log.info("Batch Job Finished: No overdue installments found.");
            return;
        }

        int updatedCount = emiScheduleRepository.updateStatusToOverdueForPastDue(currentDate);

        for (EmiSchedule emi : overdueList) {
            try {
                notificationService.sendOverdueAlert(emi.getLoan().getBorrower(), emi);

                auditService.logOfficerAction(null,
                        AuditAction.STRATEGY_OVERRIDE,
                        AuditEntityType.EMI_SCHEDULE,
                        emi.getEmiId());

            } catch (Exception e) {
                log.error("Batch Job Warning: Failed to notify/audit for EMI {}: {}", emi.getEmiId(), e.getMessage());
            }
        }

        log.info("Batch Job Finished: {} records marked as OVERDUE and processed.", updatedCount);
    }
}