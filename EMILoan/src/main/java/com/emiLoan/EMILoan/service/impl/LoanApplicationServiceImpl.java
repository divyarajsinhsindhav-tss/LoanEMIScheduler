package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.RoleName;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.engine.DtiCalculationEngine;
import com.emiLoan.EMILoan.engine.StrategySelectionEngine;
import com.emiLoan.EMILoan.entity.*;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.exceptions.ResourceNotFoundException;
import com.emiLoan.EMILoan.mapper.BorrowerProfileMapper;
import com.emiLoan.EMILoan.mapper.LoanApplicationMapper;
import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.LoanApplicationService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final LoanRepository loanRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;
    private final UserRepository userRepository;

    private final DtiCalculationEngine dtiEngine;
    private final StrategySelectionEngine strategyEngine;

    private final LoanApplicationMapper applicationMapper;
    private final BorrowerProfileMapper borrowerProfileMapper;
    private final EntityManager entityManager;

    private final NotificationService notificationService;
    private final AuditService auditService; // Added AuditService

    @Override
    @Transactional
    public LoanApplicationResponse apply(LoanApplicationRequest request, String email) {
        BorrowerProfile profile = borrowerProfileRepository.findByUser_EmailWithUser(email)
                .orElseThrow(() -> new BusinessRuleException("Borrower profile not found for email: " + email));

        Long activeLoanCount = loanRepository.countActiveLoans(profile.getUser().getUserId(), LoanStatus.ACTIVE);
        if (activeLoanCount >= AppConstants.MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException(
                    "Borrower cannot have more than " + AppConstants.MAX_ACTIVE_LOANS + " active loans at a time.");
        }

        BigDecimal dtiRatio = dtiEngine.calculate(request.getExistingEmi(), profile.getMonthlyIncome());
        String suggestedStrategy = strategyEngine.suggest(dtiRatio, request.getTenureMonths());

        LoanApplication application = applicationMapper.toEntity(request);
        application.setBorrower(profile.getUser());
        application.setDtiRatio(dtiRatio);
        application.setSuggestedStrategy(suggestedStrategy);
        application.setExistingEmi(request.getExistingEmi());

        if (AppConstants.STRATEGY_REJECTED.equalsIgnoreCase(suggestedStrategy)) {
            application.setStatus(ApplicationStatus.REJECTED);
        } else {
            application.setStatus(ApplicationStatus.PENDING);
        }

        LoanApplication savedApplication = applicationRepository.save(application);
        entityManager.flush();
        entityManager.refresh(savedApplication);
        log.info("New Loan Application {} created for borrower {}", savedApplication.getApplicationId(), email);

        try {
            if (savedApplication.getStatus() == ApplicationStatus.REJECTED) {
                notificationService.sendLoanRejected(profile.getUser(), savedApplication);
                auditService.logSystemAction(AuditAction.REJECTED, AuditEntityType.APPLICATION, savedApplication.getApplicationId());
            } else {
                notificationService.sendApplicationSubmitted(profile.getUser(), savedApplication);
                auditService.logSystemAction(AuditAction.CREATE, AuditEntityType.APPLICATION, savedApplication.getApplicationId());
            }
        } catch (Exception e) {
            log.error("Failed to send notification or audit for app {}: {}", savedApplication.getApplicationId(),
                    e.getMessage());
        }

        return applicationMapper.toResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplication(String applicationCode, String email) {
        LoanApplication loanApplication = applicationRepository
                .findByBorrowerEmailAndApplicationCode(email, applicationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationCode));

        return applicationMapper.toResponse(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationDetailsResponse> getApplications(
            String email,
            Integer pageNumber,
            Integer pageSize,
            ApplicationStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<LoanApplication> applications;

        boolean isPrivilegedUser = user.getRole().getRoleName() == RoleName.LOAN_OFFICER ||
                user.getRole().getRoleName() == RoleName.ADMIN;

        if (isPrivilegedUser) {
            applications = (status != null)
                    ? applicationRepository.findByStatus(status, pageable)
                    : applicationRepository.findAll(pageable);
        } else {
            applications = (status != null)
                    ? applicationRepository.findByBorrowerEmailAndStatus(email, status, pageable)
                    : applicationRepository.findByBorrowerEmailPaginated(email, pageable);
        }

        return applications.map(applicationMapper::toDetailsResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDetailsResponse getByApplicationCode(String applicationCode) {
        LoanApplication application = applicationRepository.findByApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessRuleException("Application not found"));

        BorrowerProfile profile = borrowerProfileRepository.findByUser_UserId(application.getBorrower().getUserId())
                .orElseThrow(() -> new BusinessRuleException("Borrower profile missing for this application"));

        return LoanApplicationDetailsResponse.builder()
                .application(applicationMapper.toResponse(application))
                .borrowerProfile(borrowerProfileMapper.toResponse(profile))
                .panFirst3(profile.getUser().getPerson().getPanFirst3())
                .panLast2(profile.getUser().getPerson().getPanLast2())
                .build();
    }
}