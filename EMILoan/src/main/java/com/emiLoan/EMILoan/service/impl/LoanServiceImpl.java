package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.*;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.LoanMapper;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.EmiService;
import com.emiLoan.EMILoan.service.interfaces.LoanService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional
    public LoanResponse processDecision(UUID appId, OfficerDecisionRequest request, String officerEmail) {
        if (request == null) {
            throw new BusinessRuleException("Decision request body is missing.");
        }

        LoanApplication application = applicationRepository.findById(appId)
                .orElseThrow(() -> new BusinessRuleException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be processed. Current status: " + application.getStatus());
        }

        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new BusinessRuleException("Officer not found"));

        UUID borrowerPersonId = application.getBorrower().getPerson().getPersonId();
        UUID officerPersonId = officer.getPerson().getPersonId();

        if (borrowerPersonId.equals(officerPersonId)) {
            log.error("Security Alert: Officer {} attempted to process their own application {}", officerEmail, appId);
            throw new BusinessRuleException("Access Denied: You cannot review your own loan application.");
        }

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

        applicationRepository.save(application);

        AuditAction action = (request.getStatus() == ApplicationStatus.APPROVED) ? AuditAction.APPROVED : AuditAction.REJECTED;
        auditService.logOfficerAction(officer, action, AuditEntityType.APPLICATION, application.getApplicationId());

        boolean isOverridden = !suggested.equals(application.getOfficerStrategy());
        auditService.logStrategyDecision(application, suggested, application.getOfficerStrategy(), isOverridden, officer);

        if (request.getStatus() == ApplicationStatus.REJECTED) {
            notificationService.sendLoanRejected(application.getBorrower(), application);
            log.info("Application {} was REJECTED by Officer {}.", appId, officerEmail);
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

        emiServicePort.generateAndSaveSchedule(savedLoan);

        log.info("Successfully generated Loan {} (Code: {}) for application {}",
                savedLoan.getLoanId(), savedLoan.getLoanCode(), application.getApplicationId());

        return loanMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse createLoanFromApplication(UUID applicationId) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessRuleException("Application not found"));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessRuleException("Cannot generate loan. Application status is: " + application.getStatus());
        }

        return generateAndPersistLoan(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans(String email) {
        return loanRepository.findByBorrowerEmail(email).stream()
                .map(loanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));
        return loanMapper.toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanSummaryResponse getLoanSummary(UUID loanId, String email) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw new BusinessRuleException("Unauthorized to view this loan summary");
        }

        LoanSummaryResponse summary = loanMapper.toSummaryResponse(loan);

        emiScheduleRepository.findFirstByLoan_LoanIdAndStatusOrderByDueDateAsc(loanId, EmiStatus.PENDING)
                .ifPresent(emi -> summary.setNextDueDate(emi.getDueDate()));

        return summary;
    }

    @Override
    @Transactional
    public LoanResponse updateLoanStatus(UUID loanId, LoanStatusUpdateRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        loanMapper.updateEntityFromStatusRequest(request, loan);

        Loan updatedLoan = loanRepository.save(loan);
        log.info("Loan {} status updated to {}", loan.getLoanCode(), updatedLoan.getLoanStatus());

        return loanMapper.toResponse(updatedLoan);
    }
}