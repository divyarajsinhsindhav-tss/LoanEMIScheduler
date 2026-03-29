package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.engine.DtiCalculationEngine;
import com.emiLoan.EMILoan.engine.StrategySelectionEngine;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.BorrowerProfileMapper;
import com.emiLoan.EMILoan.mapper.LoanApplicationMapper;
import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.LoanApplicationService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;

    private final DtiCalculationEngine dtiEngine;
    private final StrategySelectionEngine strategyEngine;

    private final LoanApplicationMapper applicationMapper;
    private final BorrowerProfileMapper borrowerProfileMapper;

    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    @Transactional
    public LoanApplicationResponse apply(LoanApplicationRequest request, String email) {
        BorrowerProfile profile = borrowerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessRuleException("Borrower profile not found for email: " + email));

        Long activeCount = applicationRepository.countActiveApplications(profile.getUser().getUserId(), ApplicationStatus.PENDING);
        if (activeCount >= AppConstants.MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException("Borrower cannot have more than " + AppConstants.MAX_ACTIVE_LOANS + " pending applications.");
        }

        BigDecimal dtiRatio = dtiEngine.calculate(request.getExistingEmi(), profile.getMonthlyIncome());
        String suggestedStrategy = strategyEngine.suggest(dtiRatio, request.getTenureMonths());

        LoanApplication application = applicationMapper.toEntity(request);
        application.setBorrower(profile.getUser());
        application.setDtiRatio(dtiRatio);
        application.setSuggestedStrategy(suggestedStrategy);
        application.setExistingEmi(request.getExistingEmi());
        application.setStatus(ApplicationStatus.PENDING);

        LoanApplication savedApplication = applicationRepository.save(application);
        log.info("New Loan Application {} created for borrower {} with DTI {} and Strategy {}",
                savedApplication.getApplicationId(), email, dtiRatio, suggestedStrategy);

        try {
            notificationService.sendApplicationSubmitted(profile.getUser(), savedApplication);
        } catch (Exception e) {
            log.error("Failed to send submission email for app {}: {}", savedApplication.getApplicationId(), e.getMessage());
        }
        auditService.logOfficerAction(null, AuditAction.CREATE, AuditEntityType.APPLICATION, savedApplication.getApplicationId());

        return applicationMapper.toResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getMyApplications(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanApplication> applicationsPage = applicationRepository.findByBorrowerEmailPaginated(email, pageable);

        return applicationsPage.map(applicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getAllPending(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "appliedAt"));
        Page<LoanApplication> pendingApps = applicationRepository.findByStatus(ApplicationStatus.PENDING, pageable);

        return pendingApps.map(applicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDetailsResponse getById(UUID applicationId) {
        LoanApplication application = applicationRepository.findById(applicationId)
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