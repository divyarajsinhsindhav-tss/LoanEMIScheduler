package com.emiLoan.EMILoan.service.impl;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.common.enums.RoleName;
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

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        EmiSchedule emi = emiScheduleRepository.findById(request.getEmiId())
                .orElseThrow(() -> new BusinessRuleException("EMI installment not found"));
        Loan loan = emi.getLoan();

        if (!loan.getBorrower().getEmail().equals(email)) {
            throw new BusinessRuleException("Unauthorized to make payments on this loan.");
        }
        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot process payment. Loan status: " + loan.getLoanStatus());
        }
        if (emi.getStatus() == EmiStatus.PAID) {
            throw new BusinessRuleException("This installment is already marked as PAID.");
        }

        EmiSchedule firstUnpaid = emiScheduleRepository
                .findFirstByLoanAndStatusNotOrderByInstallmentNoAsc(loan, EmiStatus.PAID)
                .orElse(null);

        if (firstUnpaid != null && emi.getInstallmentNo() > firstUnpaid.getInstallmentNo()) {
            throw new BusinessRuleException("Sequential payment required. Please pay installment #"
                    + firstUnpaid.getInstallmentNo() + " first.");
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

        if (paymentStatus == PaymentStatus.SUCCESS) {
            emi.setStatus(EmiStatus.PAID);
            emi.setPaidDate(LocalDate.now());
            emiScheduleRepository.save(emi);

            boolean hasUnpaidEmis = emiScheduleRepository.existsByLoanAndStatusNot(loan, EmiStatus.PAID);
            if (!hasUnpaidEmis) {
                loan.setLoanStatus(LoanStatus.CLOSED);
                loanRepository.save(loan);
                log.info("Final EMI paid. Loan {} successfully CLOSED.", loan.getLoanCode());
            }
        } else {
            log.warn("Payment FAILED for EMI ID {}", emi.getEmiId());
        }

        return paymentMapper.toResponse(savedPayment);
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

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getBorrowerPaymentHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User borrower = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        List<Loan> loans = loanRepository.findByBorrowerEmail(email);

        return loans.stream().map(loan -> {

            List<Payment> payments = paymentRepository.findByLoanOrderByPaymentDateDesc(loan);

            BigDecimal totalPaid = paymentRepository.sumSuccessfulPaymentsByLoan(loan);
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;

            return buildResponse(loan.getLoanCode(), totalPaid, payments);

        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getAllPayments() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        if (!admin.getRole().getRoleName().equals(RoleName.ADMIN)) {
            throw new BusinessRuleException("Access denied");
        }

        List<Loan> loans = loanRepository.findAll();

        return loans.stream().map(loan -> {

            List<Payment> payments = paymentRepository.findByLoanOrderByPaymentDateDesc(loan);

            BigDecimal totalPaid = paymentRepository.sumSuccessfulPaymentsByLoan(loan);
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;

            return buildResponse(loan.getLoanCode(), totalPaid, payments);

        }).collect(Collectors.toList());
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