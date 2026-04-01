package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.*;
import com.emiLoan.EMILoan.dto.payment.ForeclosureRequest;
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
            throw new BusinessRuleException("Payment rejected: Installment #" + emi.getInstallmentNo() + " is already fully paid.");
        }

        EmiSchedule firstUnpaid = emiScheduleRepository
                .findFirstByLoanAndStatusNotOrderByInstallmentNoAsc(loan, EmiStatus.PAID)
                .orElse(null);

        if (firstUnpaid != null && emi.getInstallmentNo() > firstUnpaid.getInstallmentNo()) {
            throw new BusinessRuleException("Out of sequence: Please clear installment #" + firstUnpaid.getInstallmentNo() + " first.");
        }

        BigDecimal currentAmountPaid = emi.getAmountPaid() != null ? emi.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal remainingAmount = emi.getTotalEmi().subtract(currentAmountPaid);

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero.");
        }
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessRuleException("Overpayment prevented. You only owe ₹" + remainingAmount + " for this installment.");
        }

        PaymentGatewayStrategy gateway = paymentStrategyFactory.getStrategy(request.getPaymentMode());
        PaymentStatus paymentStatus = gateway.processPayment(request.getAmount(), request.getMethodDetails());

        Payment payment = Payment.builder()
                .emiSchedule(emi)
                .loan(loan)
                .amountPaid(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        auditService.logOfficerAction(borrower, AuditAction.UPDATE, AuditEntityType.LOAN, loan.getLoanId());

        if (paymentStatus == PaymentStatus.SUCCESS) {

            notificationService.sendPaymentConfirmation(borrower, savedPayment);

            BigDecimal newTotalPaid = currentAmountPaid.add(request.getAmount());
            emi.setAmountPaid(newTotalPaid);

            if (newTotalPaid.compareTo(emi.getTotalEmi()) >= 0) {
                emi.setStatus(EmiStatus.PAID);
                emi.setPaidDate(LocalDate.now());
                log.info("EMI #{} for Loan {} is now FULLY PAID.", emi.getInstallmentNo(), loan.getLoanCode());
            } else {
                emi.setStatus(EmiStatus.PARTIALLY_PAID);
                log.info("EMI #{} for Loan {} is PARTIALLY PAID. Remaining: ₹{}",
                        emi.getInstallmentNo(), loan.getLoanCode(), emi.getTotalEmi().subtract(newTotalPaid));
            }

            emiScheduleRepository.save(emi);

            boolean hasUnpaidEmis = emiScheduleRepository.existsByLoanAndStatusNot(loan, EmiStatus.PAID);
            if (!hasUnpaidEmis) {
                loan.setLoanStatus(LoanStatus.CLOSED);
                loanRepository.save(loan);

                log.info("Loan {} has no more pending EMIs. Status updated to CLOSED.", loan.getLoanCode());
                notificationService.sendLoanClosed(borrower, loan);
            }
        } else {
            log.warn("Payment Gateway returned FAILURE for EMI ID {}", emi.getEmiId());

            notificationService.sendPaymentFailed(borrower, savedPayment);
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getPaymentHistory(String loanCode, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));

        Loan loan = loanRepository.findByLoanCode(loanCode)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        log.info("Payment history of loan {} requested by {}", loan.getLoanCode(), requester.getEmail());

        return getHistoryForLoan(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getAllPayments(String email) {
        User profile = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));
        return loanRepository.findAll().stream()
                .map(this::getHistoryForLoan)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getBorrowerPaymentHistory(String borrowerEmail) {
        User profile = userRepository.findByEmail(borrowerEmail)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));

        List<Loan> loans = loanRepository.findByBorrowerEmail(borrowerEmail);

        return loans.stream()
                .map(this::getHistoryForLoan)
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public PaymentResponse forecloseLoan(ForeclosureRequest request, String email) {
        Loan loan = loanRepository.findByLoanCode(request.getLoanCode())
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));

        if (!loan.getBorrower().getEmail().equalsIgnoreCase(email)) {
            throw new BusinessRuleException("Unauthorized: This loan does not belong to you.");
        }
        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Foreclosure rejected: Loan is currently " + loan.getLoanStatus());
        }

        EmiSchedule nextEmi = emiScheduleRepository
                .findFirstByLoanAndStatusNotOrderByInstallmentNoAsc(loan, EmiStatus.PAID)
                .orElseThrow(() -> new BusinessRuleException("No pending EMIs found. Loan might be cleared."));

        BigDecimal currentAmountPaid = nextEmi.getAmountPaid() != null ? nextEmi.getAmountPaid() : BigDecimal.ZERO;

        BigDecimal requiredForeclosureAmount = nextEmi.getRemainingBalance()
                .add(nextEmi.getPrincipalComponent())
                .add(nextEmi.getInterestComponent())
                .subtract(currentAmountPaid);

        if (request.getAmount().compareTo(requiredForeclosureAmount) < 0) {
            throw new BusinessRuleException("Foreclosure failed: Amount provided (₹" + request.getAmount() +
                    ") is less than the required settlement amount (₹" + requiredForeclosureAmount + ").");
        }

        PaymentGatewayStrategy gateway = paymentStrategyFactory.getStrategy(request.getPaymentMode());
        PaymentStatus paymentStatus = gateway.processPayment(request.getAmount(), request.getMethodDetails());

        Payment payment = Payment.builder()
                .emiSchedule(nextEmi)
                .loan(loan)
                .amountPaid(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        auditService.logOfficerAction(loan.getBorrower(), AuditAction.UPDATE, AuditEntityType.LOAN, loan.getLoanId());

        if (paymentStatus == PaymentStatus.SUCCESS) {
            log.info("Foreclosure payment of ₹{} successful for Loan {}", request.getAmount(), loan.getLoanCode());

            notificationService.sendPaymentConfirmation(loan.getBorrower(), savedPayment);

            nextEmi.setAmountPaid(currentAmountPaid.add(request.getAmount()));
            nextEmi.setStatus(EmiStatus.PAID);
            nextEmi.setPaidDate(LocalDate.now());
            emiScheduleRepository.save(nextEmi);

            List<EmiSchedule> futureEmis = emiScheduleRepository.findByLoanOrderByInstallmentNoAsc(loan).stream()
                    .filter(emi -> emi.getInstallmentNo() > nextEmi.getInstallmentNo() && emi.getStatus() != EmiStatus.PAID)
                    .collect(Collectors.toList());

            emiScheduleRepository.deleteAll(futureEmis);
            log.info("Waived and deleted {} future EMI records for foreclosed loan.", futureEmis.size());

            loan.setLoanStatus(LoanStatus.CLOSED);
            loanRepository.save(loan);

            notificationService.sendLoanClosed(loan.getBorrower(), loan);
        } else {
            log.warn("Foreclosure Payment Gateway returned FAILURE for Loan {}", loan.getLoanCode());

            notificationService.sendPaymentFailed(loan.getBorrower(), savedPayment);
        }

        return paymentMapper.toResponse(savedPayment);
    }
}