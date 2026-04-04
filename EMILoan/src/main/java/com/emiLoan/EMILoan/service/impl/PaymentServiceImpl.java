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
import com.emiLoan.EMILoan.mapper.EmiScheduleMapper;
import com.emiLoan.EMILoan.mapper.LoanMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private final LoanMapper loanMapper;
    private final EmiScheduleMapper emiScheduleMapper;

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

        Object oldEmiState = emiScheduleMapper.toResponse(emi);

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

            EmiSchedule savedEmi = emiScheduleRepository.save(emi);

            Object newEmiState = emiScheduleMapper.toResponse(savedEmi);
            auditService.logAction(
                    borrower,
                    AuditAction.UPDATE,
                    AuditEntityType.EMI_SCHEDULE,
                    savedEmi.getEmiId(),
                    "Borrower made a successful payment of ₹" + request.getAmount(),
                    oldEmiState,
                    newEmiState
            );

            boolean hasUnpaidEmis = emiScheduleRepository.existsByLoanAndStatusNot(loan, EmiStatus.PAID);
            if (!hasUnpaidEmis) {
                Object oldLoanState = loanMapper.toResponse(loan);

                loan.setLoanStatus(LoanStatus.CLOSED);
                Loan savedLoan = loanRepository.save(loan);

                Object newLoanState = loanMapper.toResponse(savedLoan);

                auditService.logAction(
                        borrower,
                        AuditAction.PAYMENT,
                        AuditEntityType.LOAN,
                        savedLoan.getLoanId(),
                        "Loan fully paid off and automatically closed",
                        oldLoanState,
                        newLoanState
                );

                log.info("Loan {} has no more pending EMIs. Status updated to CLOSED.", loan.getLoanCode());
                notificationService.sendLoanClosed(borrower, loan);
            }
        } else {
            log.warn("Payment Gateway returned FAILURE for EMI ID {}", emi.getEmiId());

            auditService.logAction(
                    borrower,
                    AuditAction.PAYMENT_FAILED,
                    AuditEntityType.PAYMENT,
                    savedPayment.getPaymentId(),
                    "Payment gateway declined/failed the transaction",
                    null,
                    paymentMapper.toResponse(savedPayment)
            );

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
    public List<PaymentHistoryResponse> getBorrowerPaymentHistory(String borrowerEmail, Pageable pageable) {
        User profile = userRepository.findByEmail(borrowerEmail)
                .orElseThrow(() -> new BusinessRuleException("User session invalid"));

        Page<Loan> loanPage = loanRepository.findByBorrowerEmail(borrowerEmail, pageable);

        return loanPage.stream()
                .map(this::getHistoryForLoan)
                .collect(Collectors.toList());
    }

    private PaymentHistoryResponse getHistoryForLoan(Loan loan) {
        Page<Payment> paymentPage = paymentRepository.findByLoanOrderByPaymentDateDesc(loan, Pageable.unpaged());

        List<Payment> payments = paymentPage.getContent();

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

        List<EmiSchedule> unpaidEmis = emiScheduleRepository
                .findAllByLoanAndStatusInOrderByInstallmentNoAsc(
                        loan,
                        List.of(EmiStatus.PENDING, EmiStatus.OVERDUE, EmiStatus.PARTIALLY_PAID),
                        Pageable.unpaged()
                ).getContent();

        if (unpaidEmis.isEmpty()) {
            throw new BusinessRuleException("Loan already cleared.");
        }

        EmiSchedule currentEmi = unpaidEmis.get(0);
        BigDecimal currentPaid = currentEmi.getAmountPaid() != null ? currentEmi.getAmountPaid() : BigDecimal.ZERO;

        BigDecimal requiredForeclosureAmount = currentEmi.getRemainingBalance()
                .add(currentEmi.getPrincipalComponent())
                .add(currentEmi.getInterestComponent())
                .subtract(currentPaid);

        if (request.getAmount().compareTo(requiredForeclosureAmount) < 0) {
            throw new BusinessRuleException(
                    "Foreclosure failed: Amount provided (₹" + request.getAmount() +
                            ") is less than the required settlement amount (₹" + requiredForeclosureAmount + ")."
            );
        }

        PaymentGatewayStrategy gateway = paymentStrategyFactory.getStrategy(request.getPaymentMode());
        PaymentStatus paymentStatus = gateway.processPayment(request.getAmount(), request.getMethodDetails());

        Payment payment = Payment.builder()
                .loan(loan)
                .emiSchedule(currentEmi)
                .amountPaid(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .status(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        Object oldLoanState = loanMapper.toResponse(loan);

        if (paymentStatus == PaymentStatus.SUCCESS) {

            log.info("Foreclosure payment of ₹{} successful for Loan {}", request.getAmount(), loan.getLoanCode());
            notificationService.sendPaymentConfirmation(loan.getBorrower(), savedPayment);

            currentEmi.setAmountPaid(currentPaid.add(request.getAmount()));
            currentEmi.setStatus(EmiStatus.PAID);
            currentEmi.setRemainingBalance(BigDecimal.ZERO);
            currentEmi.setPaidDate(LocalDate.now());
            emiScheduleRepository.save(currentEmi);

            if (unpaidEmis.size() > 1) {
                List<EmiSchedule> futureEmis = unpaidEmis.subList(1, unpaidEmis.size());
                emiScheduleRepository.deleteAll(futureEmis);
                log.info("Waived and deleted {} future EMI records for foreclosed loan.", futureEmis.size());
            }

            loan.setLoanStatus(LoanStatus.CLOSED);
            Loan savedLoan = loanRepository.save(loan);

            Object newLoanState = loanMapper.toResponse(savedLoan);

            auditService.logAction(
                    loan.getBorrower(),
                    AuditAction.PAYMENT,
                    AuditEntityType.LOAN,
                    savedLoan.getLoanId(),
                    "Borrower successfully foreclosed the loan. All future unaccrued EMIs were waived and deleted.",
                    oldLoanState,
                    newLoanState
            );

            log.info("Loan {} successfully foreclosed and closed.", loan.getLoanCode());
            notificationService.sendLoanClosed(loan.getBorrower(), loan);

        } else {
            log.warn("Foreclosure Payment FAILED for Loan {}", loan.getLoanCode());

            auditService.logAction(
                    loan.getBorrower(),
                    AuditAction.PAYMENT_FAILED,
                    AuditEntityType.PAYMENT,
                    savedPayment.getPaymentId(),
                    "Foreclosure payment gateway transaction failed",
                    null,
                    paymentMapper.toResponse(savedPayment)
            );

            notificationService.sendPaymentFailed(loan.getBorrower(), savedPayment);
        }

        return paymentMapper.toResponse(savedPayment);
    }

}