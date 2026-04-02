package com.emiLoan.EMILoan.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    public Page<EmiScheduleResponse> getSchedule(String loanCode, String requesterEmail, Pageable pageable) {
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan record not found for ID: " + loanCode));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user session invalid."));

        boolean isOwner = loan.getBorrower().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isStaff = requester.getRole().getRoleName() == RoleName.LOAN_OFFICER ||
                requester.getRole().getRoleName() == RoleName.ADMIN;

        if (!isOwner && !isStaff) {
            log.warn("Security Alert: Unauthorized access attempt to Loan {} by {}", loanCode, requesterEmail);
            throw new BusinessRuleException("Access Denied: You do not have permission to view this schedule.");
        }
        Page<EmiSchedule> schedulePage = emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan, pageable);
        return schedulePage.map(emiScheduleMapper::toResponse);
    }

    @Override
    @Transactional
    public void generateAndSaveSchedule(Loan loan,Pageable pageable) {
        if (!emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan,pageable).isEmpty()) {
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
    @Transactional(readOnly = true)
    public EmiScheduleResponse getNextUpcomingEmi(String loanCode, String requesterEmail) {
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan record not found"));

        validateAccess(loan, requesterEmail);

        EmiSchedule nextEmi = emiScheduleRepository
                .findFirstByLoanAndStatusInOrderByInstallmentNoAsc(
                        loan,
                        List.of(EmiStatus.PENDING, EmiStatus.OVERDUE, EmiStatus.PARTIALLY_PAID)
                )
                .orElseThrow(() -> new BusinessRuleException("No pending EMIs found. The loan might be fully paid."));

        return emiScheduleMapper.toResponse(nextEmi);
    }
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getForeclosureQuote(String loanCode, String requesterEmail,Pageable pageable) {

        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan record not found"));

        validateAccess(loan, requesterEmail);

        List<EmiSchedule> allEmis = emiScheduleRepository
                .findAllByLoanAndStatusInOrderByInstallmentNoAsc(
                        loan,
                        List.of(EmiStatus.PENDING, EmiStatus.OVERDUE, EmiStatus.PARTIALLY_PAID),pageable
                ).stream().collect(Collectors.toList());

        if (allEmis.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (EmiSchedule emi : allEmis) {
            BigDecimal emiAmount = emi.getPrincipalComponent()
                    .add(emi.getInterestComponent())
                    .subtract(emi.getAmountPaid() != null ? emi.getAmountPaid() : BigDecimal.ZERO);

            total = total.add(emiAmount);
        }

        return total;
    }

    private void validateAccess(Loan loan, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user session invalid."));

        boolean isOwner = loan.getBorrower().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isStaff = requester.getRole().getRoleName() == RoleName.LOAN_OFFICER ||
                requester.getRole().getRoleName() == RoleName.ADMIN;

        if (!isOwner && !isStaff) {
            log.warn("Security Alert: Unauthorized access attempt to Loan {} by {}", loan.getLoanCode(), requesterEmail);
            throw new BusinessRuleException("Access Denied: You do not have permission to view this loan data.");
        }
    }
}