package com.emiLoan.EMILoan.repository;


import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Payment;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByLoanOrderByPaymentDateDesc(Loan loan);
    List<Payment> findByEmiScheduleEmiId(UUID emiId);
    List<Payment> findByStatus(PaymentStatus status);
    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan = :loan AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByLoan(@Param("loan") Loan loan);
    boolean existsByEmiScheduleEmiIdAndStatus(UUID emiId, PaymentStatus status);
    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan.borrower = :borrower AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByBorrower(@Param("borrower") User borrower);
}