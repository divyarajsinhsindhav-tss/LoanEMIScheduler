package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.user.response.BorrowerDashboardResponse;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.BorrowerProfileMapper;
import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.service.interfaces.BorrowerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowerServiceImpl implements BorrowerService {

    private final BorrowerProfileRepository borrowerProfileRepository;
    private final BorrowerProfileMapper borrowerProfileMapper;

    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public BorrowerResponse getProfile() {
        String email = getCurrentUserEmail();
        BorrowerProfile profile = fetchProfileByEmail(email);
        return borrowerProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public BorrowerResponse updateFinancialProfile(BigDecimal newMonthlyIncome) {
        if (newMonthlyIncome == null || newMonthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Monthly income must be greater than zero.");
        }

        String email = getCurrentUserEmail();
        BorrowerProfile profile = fetchProfileByEmail(email);

        log.info("Updating income for borrower {} from {} to {}", email, profile.getMonthlyIncome(), newMonthlyIncome);
        profile.setMonthlyIncome(newMonthlyIncome);

        BorrowerProfile savedProfile = borrowerProfileRepository.save(profile);
        return borrowerProfileMapper.toResponse(savedProfile);
    }


    @Override
    @Transactional(readOnly = true)
    public BorrowerDashboardResponse getDashboardStats() {
        String email = getCurrentUserEmail();

        Long activeLoans = loanRepository.countByBorrower_EmailAndLoanStatus(email, LoanStatus.ACTIVE);
        BigDecimal totalOutstanding = loanRepository.sumRemainingTotalDebtByBorrowerEmail(email);
        Integer upcomingPayments = emiScheduleRepository.countUpcomingPayments(email, LocalDate.now().plusDays(30));

        return BorrowerDashboardResponse.builder()
                .activeLoanCount(activeLoans != null ? activeLoans : 0L)
                .totalOutstandingAmount(totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO)
                .upcomingPaymentsCount(upcomingPayments != null ? upcomingPayments : 0)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public BorrowerResponse getProfileByUserCode(String userCode) {
        // Note: Make sure to secure this endpoint with @PreAuthorize("hasAnyAuthority('ADMIN', 'LOAN_OFFICER')") in the Controller
        BorrowerProfile profile = borrowerProfileRepository.findByUser_UserCode(userCode)
                .orElseThrow(() -> new BusinessRuleException("Borrower not found with code: " + userCode));

        return borrowerProfileMapper.toResponse(profile);
    }


    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessRuleException("User not authenticated");
        }
        return authentication.getName();
    }

    private BorrowerProfile fetchProfileByEmail(String email) {
        return borrowerProfileRepository.findByUser_EmailWithUser(email)
                .orElseThrow(() -> new BusinessRuleException("Borrower profile not found for the authenticated user."));
    }
}