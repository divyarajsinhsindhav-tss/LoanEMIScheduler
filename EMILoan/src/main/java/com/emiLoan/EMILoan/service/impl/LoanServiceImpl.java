package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.common.enums.*;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.AuditLogMapper;
import com.emiLoan.EMILoan.mapper.LoanApplicationMapper;
import com.emiLoan.EMILoan.mapper.LoanMapper;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanApplicationRepository applicationRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final UserRepository userRepository;

    private final EmiService emiServicePort;
    private final LoanMapper loanMapper;


    private final NotificationService notificationService;
    private final AuditService auditService;
    private final EntityManager entityManager;

    private final AuditLogMapper auditLogMapper;
    private final LoanApplicationMapper loanApplicationMapper;

    @Override
    @Transactional
    public LoanResponse processDecision(String applicationCode, OfficerDecisionRequest request, String officerEmail) {
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new BusinessRuleException("Officer not found"));

        if (request == null) {
            throw new BusinessRuleException("Decision request body is missing.");
        }

        LoanApplication application = applicationRepository.findByApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessRuleException("Application not found"));

        if (AppConstants.STRATEGY_REJECTED.equalsIgnoreCase(application.getSuggestedStrategy())) {
            throw new BusinessRuleException("Access Denied: This application was automatically REJECTED by the system strategy engine and cannot be processed further.");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be processed. Current status: " + application.getStatus());
        }

        UUID borrowerPersonId = application.getBorrower().getPerson().getPersonId();
        UUID officerPersonId = officer.getPerson().getPersonId();

        if (borrowerPersonId.equals(officerPersonId)) {
            log.error("Security Alert: Officer {} attempted to process their own application {}", officerEmail, applicationCode);
            throw new BusinessRuleException("Access Denied: You cannot review your own loan application.");
        }

        Object oldState = loanApplicationMapper.toResponse(application);

        String suggested = application.getSuggestedStrategy();

        application.setStatus(request.getStatus());
        application.setReviewedBy(officer);
        application.setReviewedAt(LocalDateTime.now());

        if (request.getInterestRate() != null) {
            application.setInterestRate(request.getInterestRate());
        }

        if (request.getOfficerStrategy() != null && !request.getOfficerStrategy().trim().isEmpty()) {
            application.setOfficerStrategy(request.getOfficerStrategy());
        } else {
            application.setOfficerStrategy(suggested);
        }

        LoanApplication savedApplication = applicationRepository.save(application);

        Object newState = loanApplicationMapper.toResponse(savedApplication);

        AuditAction action = (request.getStatus() == ApplicationStatus.APPROVED) ? AuditAction.APPROVED : AuditAction.REJECTED;
        String auditDescription = (request.getStatus() == ApplicationStatus.APPROVED)
                ? "Loan Officer approved the application and assigned an interest rate."
                : "Loan Officer rejected the application.";

        auditService.logAction(
                officer,
                action,
                AuditEntityType.APPLICATION,
                savedApplication.getApplicationId(),
                auditDescription,
                oldState,
                newState
        );

        boolean isOverridden = !suggested.equals(application.getOfficerStrategy());
        auditService.logStrategyDecision(application, suggested, application.getOfficerStrategy(), isOverridden, officer);

        if (request.getStatus() == ApplicationStatus.REJECTED) {
            notificationService.sendLoanRejected(application.getBorrower(), application);
            log.info("Application {} was REJECTED by Officer {}.", applicationCode, officerEmail);
            return null;
        }

        if (request.getStatus() == ApplicationStatus.APPROVED) {
            if (application.getInterestRate() == null) {
                throw new BusinessRuleException("Interest rate must be assigned before approval.");
            }

            LoanResponse loanResponse = generateAndPersistLoan(application);

            Loan savedLoan = loanRepository.findById(loanResponse.getLoanId())
                    .orElseThrow(() -> new BusinessRuleException("Internal Error: Loan record not found after generation."));

            notificationService.sendLoanApproved(application.getBorrower(), savedLoan);

            return loanResponse;
        }

        throw new BusinessRuleException("Invalid decision status provided.");
    }

    private LoanResponse generateAndPersistLoan(LoanApplication application) {
        if (loanRepository.findByApplicationId(application.getApplicationId()).isPresent()) {
            throw new BusinessRuleException("A loan has already been generated for this application.");
        }

        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusMonths(application.getTenureMonths() - 1);

        Loan loan = Loan.builder()
                .application(application)
                .borrower(application.getBorrower())
                .principalAmount(application.getRequestedAmount())
                .interestRate(application.getInterestRate())
                .tenureMonths(application.getTenureMonths())
                .strategy(application.getOfficerStrategy())
                .startDate(startDate)
                .endDate(endDate)
                .loanStatus(LoanStatus.ACTIVE)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        entityManager.flush();
        entityManager.refresh(savedLoan);

        emiServicePort.generateAndSaveSchedule(savedLoan,Pageable.unpaged());

        LoanResponse responseDto = loanMapper.toResponse(savedLoan);

        auditService.logAction(
                application.getReviewedBy(),
                AuditAction.CREATE,
                AuditEntityType.LOAN,
                savedLoan.getLoanId(),
                "Loan account officially generated and activated following application approval",
                null,
                responseDto
        );

        log.info("Successfully generated Loan {} (Code: {}) for application {}",
                savedLoan.getLoanId(), savedLoan.getLoanCode(), application.getApplicationId());

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> getMyLoans(String email,Pageable pageable) {
        return loanRepository.findByBorrowerEmail(email,pageable).map(loanMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> getAllLoans(String requesterEmail, int pageNumber, int pageSize, LoanStatus status) {
        verifyAdminOrOfficerPrivileges(requesterEmail);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Loan> loans;

        if (status != null) {
            loans = loanRepository.findByLoanStatus(status, pageable);
        } else {
            loans = loanRepository.findAll(pageable);
        }

        return loans.map(loanMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoan(String loanCode, String email) {
        User profile = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));
        return loanMapper.toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanSummaryResponse getLoanSummary(String loanCode, String email) {
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw new BusinessRuleException("Unauthorized to view this loan summary");
        }

        LoanSummaryResponse summary = loanMapper.toSummaryResponse(loan);

        emiScheduleRepository.findFirstByLoan_LoanCodeAndStatusOrderByDueDateAsc(loanCode, EmiStatus.PENDING)
                .ifPresent(emi -> summary.setNextDueDate(emi.getDueDate()));

        return summary;
    }

    @Override
    @Transactional
    public LoanResponse updateLoanStatus(String loanCode, LoanStatusUpdateRequest request) {
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        LoanResponse oldState = loanMapper.toResponse(loan);

        loanMapper.updateEntityFromStatusRequest(request, loan);
        Loan updatedLoan = loanRepository.save(loan);

        LoanResponse newState = loanMapper.toResponse(updatedLoan);

        User currentActor = getAuthenticatedActor();

        auditService.logAction(
                currentActor,
                AuditAction.UPDATE,
                AuditEntityType.LOAN,
                updatedLoan.getLoanId(),
                "Staff manually updated loan status to " + updatedLoan.getLoanStatus(),
                oldState,
                newState
        );

        log.info("Loan {} status updated to {} by user {}",
                loan.getLoanCode(), updatedLoan.getLoanStatus(),
                currentActor != null ? currentActor.getEmail() : "SYSTEM");

        return newState;
    }

    private User getAuthenticatedActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLoanAuditHistory(String loanCode, String requesterEmail,Pageable pageable) {
        verifyAdminOrOfficerPrivileges(requesterEmail);
        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));
        return auditService.getEntityAuditHistory(AuditEntityType.LOAN, loan.getLoanId(),pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StrategyAuditResponse> getStrategyOverrides(String requesterEmail,Pageable pageable) {
        verifyAdminOrOfficerPrivileges(requesterEmail);
        return auditService.getRecentStrategyOverrides(pageable);
    }

    private void verifyAdminOrOfficerPrivileges(String email) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user session invalid."));

        if (requester.getRole().getRoleName() != RoleName.LOAN_OFFICER &&
                requester.getRole().getRoleName() != RoleName.ADMIN) {
            throw new BusinessRuleException("Access Denied: You do not have permission to perform this action.");
        }
    }
}