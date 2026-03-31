package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.engine.DtiCalculationEngine;
import com.emiLoan.EMILoan.engine.StrategySelectionEngine;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.LoanApplicationMapper;
import com.emiLoan.EMILoan.repository.BorrowerProfileRepository;
import com.emiLoan.EMILoan.repository.LoanApplicationRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService; // ✅ FIXED

    @Mock
    private BorrowerProfileRepository borrowerProfileRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private DtiCalculationEngine dtiEngine;

    @Mock
    private StrategySelectionEngine strategyEngine;

    @Mock
    private LoanApplicationMapper applicationMapper;

    @Mock
    private LoanApplicationRepository applicationRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @Test
    void shouldApplyLoanSuccessfully_whenValidRequest() {

        // Arrange
        String email = "test@example.com";

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setExistingEmi(BigDecimal.valueOf(5000));
        request.setTenureMonths(12);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        BorrowerProfile profile = new BorrowerProfile();
        profile.setUser(user);
        profile.setMonthlyIncome(BigDecimal.valueOf(50000));

        LoanApplication application = new LoanApplication();

        UUID applicationId = UUID.randomUUID();
        LoanApplication savedApplication = new LoanApplication();
        savedApplication.setApplicationId(applicationId);
        savedApplication.setStatus(ApplicationStatus.PENDING);

        // Mock behavior
        when(borrowerProfileRepository.findByUser_EmailWithUser(email))
                .thenReturn(Optional.of(profile));

        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE))
                .thenReturn(0L);

        when(dtiEngine.calculate(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(0.2));

        when(strategyEngine.suggest(any(BigDecimal.class), anyInt()))
                .thenReturn("APPROVED");

        when(applicationMapper.toEntity(request))
                .thenReturn(application);

        when(applicationRepository.save(application))
                .thenReturn(savedApplication);

        when(applicationMapper.toResponse(savedApplication))
                .thenReturn(new LoanApplicationResponse());

        // Act
        LoanApplicationResponse response =
                loanApplicationService.apply(request, email);

        // Assert
        assertNotNull(response);

        verify(notificationService).sendApplicationSubmitted(user, savedApplication);

        verify(auditService).logSystemAction(
                eq(AuditAction.CREATE),
                eq(AuditEntityType.APPLICATION),
                eq(applicationId)
        );

        // Optional verifications
        verify(applicationRepository).save(application);
        verify(entityManager).flush();
        verify(entityManager).refresh(savedApplication);
    }

    @Test
    void shouldRejectLoan_whenStrategyIsRejected() {

        // Arrange
        String email = "test@example.com";

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setExistingEmi(BigDecimal.valueOf(5000));
        request.setTenureMonths(12);

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        BorrowerProfile profile = new BorrowerProfile();
        profile.setUser(user);
        profile.setMonthlyIncome(BigDecimal.valueOf(50000));

        LoanApplication application = new LoanApplication();

        UUID applicationId = UUID.randomUUID();
        LoanApplication savedApplication = new LoanApplication();
        savedApplication.setApplicationId(applicationId);
        savedApplication.setStatus(ApplicationStatus.REJECTED);

        // Mock behavior
        when(borrowerProfileRepository.findByUser_EmailWithUser(email))
                .thenReturn(Optional.of(profile));

        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE))
                .thenReturn(0L);

        when(dtiEngine.calculate(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(0.9)); // High DTI → rejection

        when(strategyEngine.suggest(any(BigDecimal.class), anyInt()))
                .thenReturn("REJECTED");

        when(applicationMapper.toEntity(request))
                .thenReturn(application);

        when(applicationRepository.save(application))
                .thenReturn(savedApplication);

        when(applicationMapper.toResponse(savedApplication))
                .thenReturn(new LoanApplicationResponse());

        // Act
        LoanApplicationResponse response =
                loanApplicationService.apply(request, email);

        // Assert
        assertNotNull(response);

        // Verify rejection notification
        verify(notificationService).sendLoanRejected(user, savedApplication);

        // Ensure success notification NOT called
        verify(notificationService, never()).sendApplicationSubmitted(any(), any());

        // Verify audit log
        verify(auditService).logSystemAction(
                eq(AuditAction.REJECTED),
                eq(AuditEntityType.APPLICATION),
                eq(applicationId)
        );

        // Optional verifications
        verify(applicationRepository).save(application);
        verify(entityManager).flush();
        verify(entityManager).refresh(savedApplication);
    }

    @Test
    void shouldThrowException_whenMaxActiveLoansExceeded() {

        // Arrange
        String email = "test@example.com";

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);

        BorrowerProfile profile = new BorrowerProfile();
        profile.setUser(user);

        // Mock behavior
        when(borrowerProfileRepository.findByUser_EmailWithUser(email))
                .thenReturn(Optional.of(profile));

        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE))
                .thenReturn((long) AppConstants.MAX_ACTIVE_LOANS); // limit reached

        assertThrows(BusinessRuleException.class, () ->
                loanApplicationService.apply(new LoanApplicationRequest(), email)
        );

        // Ensure nothing else is called after failure
        verify(applicationRepository, never()).save(any());
        verify(notificationService, never()).sendApplicationSubmitted(any(), any());
        verify(notificationService, never()).sendLoanRejected(any(), any());
        verify(auditService, never()).logSystemAction(any(), any(), any());
    }
}