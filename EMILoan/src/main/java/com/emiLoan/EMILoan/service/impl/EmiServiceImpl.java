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
    public List<EmiScheduleResponse> getSchedule(UUID loanId, String email) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        boolean isOwner = loan.getBorrower().getEmail().equals(email);
        boolean isPrivileged = requester.getRole().getRoleName() == RoleName.LOAN_OFFICER ||
                requester.getRole().getRoleName() == RoleName.ADMIN;

        if (!isOwner && !isPrivileged) {
            log.warn("Unauthorized schedule access attempt by {} for Loan {}", email, loanId);
            throw new BusinessRuleException("Unauthorized access to loan schedule.");
        }

        List<EmiSchedule> scheduleList = emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan);
        return emiScheduleMapper.toResponseList(scheduleList);
    }

    @Override
    @Transactional
    public void generateAndSaveSchedule(Loan loan) {
        if (!emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan).isEmpty()) {
            log.warn("Schedule already exists for Loan {}. Generation aborted.", loan.getLoanCode());
            return;
        }

        List<EmiSchedule> schedules = amortizationEngine.buildSchedule(loan);

        if (schedules == null || schedules.isEmpty()) {
            throw new BusinessRuleException("Amortization Engine failed to generate rows for Loan: " + loan.getLoanCode());
        }

        emiScheduleRepository.saveAll(schedules);

        BigDecimal baseEmi = schedules.get(0).getTotalEmi();
        loan.setEmiAmount(baseEmi);
        loanRepository.save(loan);

        log.info("Schedule Generated: {} installments for Loan {}", schedules.size(), loan.getLoanCode());
    }

    @Override
    @Transactional
    public void processOverdueEmis(LocalDate currentDate) {
        log.info("Cron Execution: Overdue detection for date {}", currentDate);

        List<EmiSchedule> overdueList = emiScheduleRepository.findByDueDateBeforeAndStatus(currentDate, EmiStatus.PENDING);

        if (overdueList.isEmpty()) {
            log.info("No pending installments found past their due date.");
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
                log.error("Notification/Audit error for Overdue EMI {}: {}", emi.getEmiId(), e.getMessage());
            }
        }

        log.info("Cleanup Complete: {} installments marked as OVERDUE.", updatedCount);
    }
}