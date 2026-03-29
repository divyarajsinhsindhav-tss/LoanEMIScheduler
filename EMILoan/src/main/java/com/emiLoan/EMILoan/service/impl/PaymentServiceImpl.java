package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.*;
import com.emiLoan.EMILoan.dto.payment.PaymentHistoryResponse;
import com.emiLoan.EMILoan.dto.payment.PaymentRequest;
import com.emiLoan.EMILoan.dto.payment.PaymentResponse;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Payment;
import com.emiLoan.EMILoan.entity.User;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import com.emiLoan.EMILoan.mapper.PaymentMapper;
import com.emiLoan.EMILoan.repository.EmiScheduleRepository;
import com.emiLoan.EMILoan.repository.LoanRepository;
import com.emiLoan.EMILoan.repository.PaymentRepository;
import com.emiLoan.EMILoan.repository.UserRepository;
import com.emiLoan.EMILoan.service.interfaces.AuditService;
import com.emiLoan.EMILoan.service.interfaces.NotificationService;
import com.emiLoan.EMILoan.service.interfaces.PaymentService;
import com.emiLoan.EMILoan.strategy.payment.PaymentGatewayStrategy;
import com.emiLoan.EMILoan.strategy.payment.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private final PaymentStrategyFactory paymentStrategyFactory;
    private final PaymentMapper paymentMapper;

    private final NotificationService notificationService;
    private final AuditService auditService;


    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request, String email) {
        EmiSchedule emi = emiScheduleRepository.findById(request.getEmiId())
                .orElseThrow(() -> new BusinessRuleException("EMI installment not found"));

        Loan loan = emi.getLoan();
        User borrower = loan.getBorrower();

        if (!borrower.getEmail().equalsIgnoreCase(email)) {
            throw new BusinessRuleException("Unauthorized: This loan does not belong to you.");
        }
        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Payment rejected: Loan is currently " + loan.getLoanStatus());
        }
        if (emi.getStatus() == EmiStatus.PAID) {
            throw new BusinessRuleException("Payment rejected: Installment #" + emi.getInstallmentNo() + " is already paid.");
        }

        EmiSchedule firstUnpaid = emiScheduleRepository
                .findFirstByLoanAndStatusNotOrderByInstallmentNoAsc(loan, EmiStatus.PAID)
                .orElse(null);

        if (firstUnpaid != null && emi.getInstallmentNo() > firstUnpaid.getInstallmentNo()) {
            throw new BusinessRuleException("Out of sequence: Please pay installment #" + firstUnpaid.getInstallmentNo() + " first.");
        }

        PaymentGatewayStrategy gateway = paymentStrategyFactory.getStrategy(request.getPaymentMode());
        PaymentStatus paymentStatus = gateway.processPayment(emi.getTotalEmi());

        Payment payment = Payment.builder()
                .emiSchedule(emi)
                .loan(loan)
                .amountPaid(emi.getTotalEmi())
                .paymentMode(request.getPaymentMode())
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        auditService.logOfficerAction(null, AuditAction.PAYMENT, AuditEntityType.LOAN, loan.getLoanId());

        if (paymentStatus == PaymentStatus.SUCCESS) {
            emi.setStatus(EmiStatus.PAID);
            emi.setPaidDate(LocalDate.now());
            emiScheduleRepository.save(emi);

            log.info("EMI #{} for Loan {} successfully paid.", emi.getInstallmentNo(), loan.getLoanCode());

            boolean hasUnpaidEmis = emiScheduleRepository.existsByLoanAndStatusNot(loan, EmiStatus.PAID);
            if (!hasUnpaidEmis) {
                loan.setLoanStatus(LoanStatus.CLOSED);
                loanRepository.save(loan);

                log.info("Loan {} has no more pending EMIs. Status updated to CLOSED.", loan.getLoanCode());
                notificationService.sendLoanClosed(borrower, loan);
            }
        } else {
            log.warn("Payment Gateway returned FAILURE for EMI ID {}", emi.getEmiId());
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getPaymentHistory(UUID loanId, String requesterEmail) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));

        boolean isOwner = loan.getBorrower().getEmail().equalsIgnoreCase(requesterEmail);
        boolean isStaff = requester.getRole().getRoleName() == RoleName.LOAN_OFFICER ||
                requester.getRole().getRoleName() == RoleName.ADMIN;

        if (!isOwner && !isStaff) {
            throw new BusinessRuleException("Access Denied: You cannot view this payment history.");
        }

        return getHistoryForLoan(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getAllPayments() {
        return loanRepository.findAll().stream()
                .map(this::getHistoryForLoan)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getBorrowerPaymentHistory(String borrowerEmail) {
        List<Loan> loans = loanRepository.findByBorrowerEmail(borrowerEmail);

        return loans.stream()
                .map(this::getHistoryForLoan)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getLoanPaymentHistory(String loanId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Loan loan = loanRepository.findByLoanCode(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw new BusinessRuleException("Unauthorized access");
        }

        List<Payment> payments = paymentRepository.findByLoanOrderByPaymentDateDesc(loan);

        BigDecimal totalPaid = paymentRepository.sumSuccessfulPaymentsByLoan(loan);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        return List.of(buildResponse(loan.getLoanCode(), totalPaid, payments));
    }

    private PaymentHistoryResponse getHistoryForLoan(Loan loan) {
        List<Payment> payments = paymentRepository.findByLoanOrderByPaymentDateDesc(loan);
        BigDecimal totalPaid = paymentRepository.sumSuccessfulPaymentsByLoan(loan);

        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        List<PaymentResponse> transactions = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());

        return PaymentHistoryResponse.builder()
                .loanCode(loan.getLoanCode())
                .totalAmountPaid(totalPaid)
                .transactions(transactions)
                .build();
    }
    private PaymentHistoryResponse buildResponse(
            String loanCode,
            BigDecimal totalPaid,
            List<Payment> payments
    ) {

        List<PaymentResponse> transactionResponses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());

        return PaymentHistoryResponse.builder()
                .loanCode(loanCode)
                .totalAmountPaid(totalPaid)
                .transactions(transactionResponses)
                .build();
    }
}