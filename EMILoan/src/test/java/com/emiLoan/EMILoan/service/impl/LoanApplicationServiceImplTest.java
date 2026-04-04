package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.constants.AppConstants;
import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.common.enums.AuditAction;
import com.emiLoan.EMILoan.common.enums.AuditEntityType;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationSubmitResponse;
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
    private LoanApplicationServiceImpl loanApplicationService;

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
        String email = "test@example.com";

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setRequestedAmount(BigDecimal.valueOf(100000));
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

        when(borrowerProfileRepository.findByUser_EmailWithUser(email)).thenReturn(Optional.of(profile));
        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE)).thenReturn(0L);
        when(applicationRepository.countByBorrower_UserIdAndStatus(userId, ApplicationStatus.PENDING)).thenReturn(0L); // ✅ Strict check mock

        when(dtiEngine.calculate(any(), any())).thenReturn(BigDecimal.valueOf(0.2));
        when(strategyEngine.suggest(any(), anyInt())).thenReturn("APPROVED");
        when(applicationMapper.toEntity(request)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(savedApplication);

        when(applicationMapper.toSubmitResponse(savedApplication)).thenReturn(new LoanApplicationSubmitResponse());

        LoanApplicationSubmitResponse response = loanApplicationService.apply(request, email);

        assertNotNull(response);
        verify(notificationService).sendApplicationSubmitted(user, savedApplication);
    }

    @Test
    void shouldRejectLoan_whenStrategyIsRejected() {
        // Arrange
        String email = "test@example.com";

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setRequestedAmount(BigDecimal.valueOf(100000)); // ✅ FIXED
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

        when(borrowerProfileRepository.findByUser_EmailWithUser(email)).thenReturn(Optional.of(profile));
        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE)).thenReturn(0L);
        when(applicationRepository.countByBorrower_UserIdAndStatus(userId, ApplicationStatus.PENDING)).thenReturn(0L);

        when(dtiEngine.calculate(any(), any())).thenReturn(BigDecimal.valueOf(0.9));
        when(strategyEngine.suggest(any(), anyInt())).thenReturn("REJECTED");
        when(applicationMapper.toEntity(request)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(savedApplication);
        when(applicationMapper.toSubmitResponse(savedApplication)).thenReturn(new LoanApplicationSubmitResponse());

        LoanApplicationSubmitResponse response = loanApplicationService.apply(request, email);

        assertNotNull(response);
        verify(notificationService).sendLoanRejected(user, savedApplication);
    }

    @Test
    void shouldThrowException_whenMaxActiveLoansExceeded() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        BorrowerProfile profile = new BorrowerProfile();
        profile.setUser(user);

        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setRequestedAmount(BigDecimal.valueOf(100000)); // ✅ FIXED
        request.setTenureMonths(12);

        when(borrowerProfileRepository.findByUser_EmailWithUser(email)).thenReturn(Optional.of(profile));

        when(loanRepository.countActiveLoans(userId, LoanStatus.ACTIVE)).thenReturn(1L);
        when(applicationRepository.countByBorrower_UserIdAndStatus(userId, ApplicationStatus.PENDING)).thenReturn(2L);

        assertThrows(BusinessRuleException.class, () -> loanApplicationService.apply(request, email));
    }
}