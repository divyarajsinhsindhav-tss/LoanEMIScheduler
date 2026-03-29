package com.emiLoan.EMILoan.repository;

import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.Payment;
import com.emiLoan.EMILoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByLoanOrderByPaymentDateDesc(Loan loan);

    List<Payment> findByLoan_BorrowerOrderByPaymentDateDesc(User borrower);

    List<Payment> findAllByOrderByPaymentDateDesc();

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan = :loan AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByLoan(@Param("loan") Loan loan);

    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.loan.borrower = :borrower AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByBorrower(@Param("borrower") User borrower);
}