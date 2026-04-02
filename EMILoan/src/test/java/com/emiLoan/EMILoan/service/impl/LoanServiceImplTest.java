package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.entity.*;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.AuditLogMapper;
import com.emiLoan.EMILoan.mapper.LoanMapper;
import com.emiLoan.EMILoan.repository.*;
import com.emiLoan.EMILoan.service.interfaces.*;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @InjectMocks
    private LoanServiceImpl loanService;

    @Mock private LoanRepository loanRepository;
    @Mock private LoanApplicationRepository applicationRepository;
    @Mock private EmiScheduleRepository emiScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmiService emiServicePort;
    @Mock private LoanMapper loanMapper;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private EntityManager entityManager;
    @Mock private AuditLogMapper auditLogMapper;

    private User officer;
    private LoanApplication application;

    @BeforeEach
    void setup() {
        // Officer
        officer = new User();
        officer.setEmail("officer@test.com");

        PersonIdentity officerPerson = new PersonIdentity();
        officerPerson.setPersonId(UUID.randomUUID());
        officer.setPerson(officerPerson);

        // Borrower
        User borrower = new User();
        borrower.setEmail("borrower@test.com");

        PersonIdentity borrowerPerson = new PersonIdentity();
        borrowerPerson.setPersonId(UUID.randomUUID());
        borrower.setPerson(borrowerPerson);

        // Application
        application = new LoanApplication();
        application.setApplicationId(UUID.randomUUID());
        application.setApplicationCode("APP123");
        application.setStatus(ApplicationStatus.PENDING);
        application.setSuggestedStrategy("AUTO_APPROVE");
        application.setBorrower(borrower);
        application.setRequestedAmount(new BigDecimal(100000.0));
        application.setTenureMonths(12);
    }

    // APPROVED
    @Test
    void processDecision_shouldApproveAndCreateLoan() {

        OfficerDecisionRequest request = new OfficerDecisionRequest();
        request.setStatus(ApplicationStatus.APPROVED);
        request.setInterestRate(new BigDecimal(10.0));

        Loan loan = Loan.builder()
                .loanId(UUID.randomUUID())
                .loanCode("LN001")
                .build();

        LoanResponse response = new LoanResponse();
        response.setLoanId(loan.getLoanId());

        when(userRepository.findByEmail("officer@test.com"))
                .thenReturn(Optional.of(officer));

        when(applicationRepository.findByApplicationCode("APP123"))
                .thenReturn(Optional.of(application));

        when(loanRepository.findByApplicationId(application.getApplicationId()))
                .thenReturn(Optional.empty());

        when(loanRepository.save(any())).thenReturn(loan);
        when(loanRepository.findById(any())).thenReturn(Optional.of(loan));
        when(loanMapper.toResponse(any())).thenReturn(response);

        LoanResponse result =
                loanService.processDecision("APP123", request, "officer@test.com");

        assertNotNull(result);
        verify(notificationService).sendLoanApproved(any(), any());
        verify(emiServicePort).generateAndSaveSchedule(any());
    }

    @Test
    void processDecision_shouldRejectApplication() {

        OfficerDecisionRequest request = new OfficerDecisionRequest();
        request.setStatus(ApplicationStatus.REJECTED);

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(officer));

        when(applicationRepository.findByApplicationCode(any()))
                .thenReturn(Optional.of(application));

        LoanResponse result =
                loanService.processDecision("APP123", request, "officer@test.com");

        assertNull(result);
        verify(notificationService).sendLoanRejected(any(), any());
    }

    // ERROR CASE
    @Test
    void processDecision_shouldThrowException_whenRequestNull() {

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(officer));

        assertThrows(BusinessRuleException.class, () ->
                loanService.processDecision("APP123", null, "officer@test.com"));
    }
}